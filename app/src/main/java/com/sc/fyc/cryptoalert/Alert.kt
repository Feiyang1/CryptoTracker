package com.sc.fyc.cryptoalert

class Alert(val symbol: String, val price: Double, val type: String) {
    constructor(): this("", 0.0, "")
}