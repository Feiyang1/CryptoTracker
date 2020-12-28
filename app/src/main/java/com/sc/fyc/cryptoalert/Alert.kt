package com.sc.fyc.cryptoalert

class Alert(val symbol: String, val price: Double, val type: String, val triggered: Boolean) {
    constructor(): this("", 0.0, "", false)
}