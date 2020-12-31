package com.sc.fyc.cryptoalert

import com.google.firebase.Timestamp

class Alert(val symbol: String, val id: String, val price: Double, val type: String, val triggered: Boolean, val triggered_timestamp: Timestamp?) {
    constructor(): this("", "", 0.0, "", false, null)
}