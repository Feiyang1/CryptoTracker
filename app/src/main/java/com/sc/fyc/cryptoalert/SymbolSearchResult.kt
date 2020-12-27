package com.sc.fyc.cryptoalert

import org.json.JSONObject

class SymbolSearchResult(var symbol: String, var price: Double) {
    constructor(remoteResult: SymbolSearchResultRemote): this(remoteResult.symbol, remoteResult.price) {
    }
}

class SymbolSearchResultRemote(json: String): JSONObject(json) {
    val symbol = this.optString("symbol")
    val price = this.optDouble("priceUsd")
}