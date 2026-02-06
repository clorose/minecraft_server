package io.clorose.wolfOfMinestreet.manager

import io.clorose.wolfOfMinestreet.model.Stock

class StockManager {
    private val stocks = mutableMapOf<String, Stock>()

    fun registerStock(stock: Stock) {
        stocks[stock.id] = stock
    }

    fun getStock(id: String): Stock? {
        return stocks[id]
    }

    fun getStockBySymbol(symbol: String): Stock? {
        return stocks.values.find { it.symbol.equals(symbol, ignoreCase = true) }
    }

    fun getAllStocks(): List<Stock> {
        return stocks.values.toList()
    }

    fun updatePrice(stockId: String, newPrice: Double): Boolean {
        val stock = stocks[stockId] ?: return false
        stock.currentPrice = newPrice
        return true
    }
}
