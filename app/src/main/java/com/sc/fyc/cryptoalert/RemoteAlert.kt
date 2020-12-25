package com.sc.fyc.cryptoalert

class RemoteAlertDoc (val userId: String, val alerts: List<Alert>) {
    constructor(): this("", listOf())
}

