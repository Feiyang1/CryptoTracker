package com.sc.fyc.cryptoalert

class Alert(val symbol: String, val price: Double) {
    constructor(): this("", 0.0)
}