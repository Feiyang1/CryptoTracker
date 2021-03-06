import * as functions from 'firebase-functions';
import * as firebase from 'firebase-admin';
import fetch from 'node-fetch';
import { createTransport, SendMailOptions } from 'nodemailer';

firebase.initializeApp();

const transporter = createTransport({
    service: 'gmail',
    auth: {
        user: functions.config().email.address,
        pass: functions.config().email.pwd,
    },
});

// // Start writing Firebase Functions
// // https://firebase.google.com/docs/functions/typescript
//
export const runAlertsOnce = functions.https.onRequest(async (request, response) => {
    functions.logger.info("Hello logs!", { structuredData: true });

    await runAlerts();

    response.send("Hello from Firebase!");
});

export const secheduledAlerts = functions.pubsub.schedule('every 5 minutes').onRun(async (context) => {
    console.log('This will be run every 5 minutes!');
    await runAlerts();
});

async function runAlerts(): Promise<void> {
    const alerts = await getAlerts();
    const symbolIds = getDistinctSymbolIds(alerts);

    const prices = await getPrices(symbolIds);

    // trigger alerts
    for (const alert of alerts) {
        const currentPrice = prices[alert.id]?.price;

        switch (alert.type) {
            case 'above':
                if (currentPrice >= alert.price) {
                    await sendAlert(alert, currentPrice);
                    updateTriggered(alert).catch(err => console.log('updating triggered failed:', err));
                }
                break;
            case 'below':
                if (currentPrice <= alert.price) {
                    await sendAlert(alert, currentPrice);
                    updateTriggered(alert).catch(err => console.log('updating triggered failed:', err));
                }
                break;
            default:
                throw Error(`impossible alert type: ${alert.type}`);
        }
    }
}

function sendAlert(alert: Alert, currentPrice: number): void {
    const message = generateAlertMessage(alert, currentPrice);
    console.log(message);


    const mailOptions: SendMailOptions = {
        from: 'The awesome guy',
        to: alert.sendTo,
        subject: 'Crypto Alert',
        html: `<p>${message}</p>`,
    }


    transporter.sendMail(mailOptions, (err, info) => {
        if (err) {
            console.log('sending email failed', err);
        }
    });
}

function updateTriggered(alert: Alert): Promise<firebase.firestore.WriteResult> {
    return alert.docRef.update({
        triggered: true,
        triggered_timestamp: firebase.firestore.FieldValue.serverTimestamp(),
    });
}

function generateAlertMessage(alert: Alert, currentPrice: number): string {
    switch (alert.type) {
        case 'above':
            return `${alert.symbol} went over ${alert.price} at ${currentPrice}`;
        case 'below':
            return `${alert.symbol} went below ${alert.price} at ${currentPrice}`;
        default:
            throw Error(`impossible alert type: ${alert.type}`);
    }
}

async function getAlerts(): Promise<Alert[]> {
    const alerts: Alert[] = [];
    const querySnapshot = await firebase.firestore().collectionGroup('alerts').where('triggered', '==', false).get();

    const alertPromises: Promise<void>[] = [];
    querySnapshot.forEach(doc => {
        const alert = doc.data() as AlertFirestore;

        alertPromises.push(
            getUserEmail(doc.ref.path).then(email => {
                if (email) {
                    alerts.push({
                        symbol: alert.symbol,
                        id: alert.id,
                        price: alert.price,
                        type: alert.type,
                        sendTo: email,
                        docRef: doc.ref,
                    });
                }
            })
        );
    });

    await Promise.all(alertPromises);
    return alerts;
}

function getDistinctSymbolIds(alerts: Alert[]): Set<string> {
    const distinctSymbols = new Set<string>();
    for (const alert of alerts) {
        // use id since the api for querying price takes id instead of symbol
        distinctSymbols.add(alert.id);
    }
    return distinctSymbols;
}

// return a map for easy lookup. The key is the id of the asset
async function getPrices(ids: Set<string>): Promise<Record<string, PriceInfo>> {
    const idArray = [];
    for (const id of ids.values()) {
        idArray.push(id)
    }

    const queryParameters = `ids=${idArray.join(',')}`;
    const response = await fetch(`https://api.coincap.io/v2/assets?${queryParameters}`);
    const json = await response.json();

    const priceMap: Record<string, PriceInfo> = {};
    if (json.data) {
        for (const price of json.data as CoinCapAsset[]) {
            priceMap[price.id] = {
                symbol: price.symbol,
                id: price.id,
                price: Number(price.priceUsd),
            };
        }
    }

    return priceMap;
}

/**
 * 
 * @param alertPath document path in firestore in the format of users/[userid]/alerts/myrandomalertid
 * we are going to use the userid to get the email address
 */
async function getUserEmail(alertPath: string): Promise<string | undefined> {
    // get userid from doc path 'users/[userid]/alerts/myrandomalertid'
    const userId = alertPath.split('/')[1];
    const user = await firebase.auth().getUser(userId);

    return user.email;
}

interface PriceInfo {
    symbol: string;
    id: string;
    price: number;
}
interface Alert {
    symbol: string;
    id: string;
    price: number;
    type: 'above' | 'below',
    sendTo: string;
    docRef: firebase.firestore.DocumentReference; // It will be used to update `triggered` field after an alert is triggered
}

interface AlertFirestore {
    symbol: string;
    id: string;
    price: number;
    type: 'above' | 'below',
    triggered: boolean
}

interface CoinCapAsset {
    "id": string,
    "rank": string,
    "symbol": string,
    "name": string,
    "supply": string,
    "maxSupply": string,
    "marketCapUsd": string,
    "volumeUsd24Hr": string,
    "priceUsd": string,
    "changePercent24Hr": string,
    "vwap24Hr": string
}