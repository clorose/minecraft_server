package io.clorose.wolfOfMinestreet.manager

import io.clorose.wolfOfMinestreet.model.Stock
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

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

    fun removeStock(id: String) {
        stocks.remove(id)
    }

    fun updatePrice(stockId: String, newPrice: Double): Boolean {
        val stock = stocks[stockId] ?: return false
        stock.currentPrice = newPrice
        return true
    }

    fun isEmpty(): Boolean = stocks.isEmpty()

    fun save(dataFolder: File) {
        val config = YamlConfiguration()
        stocks.forEach { (id, stock) ->
            config.set("stocks.$id.currentPrice", stock.currentPrice)
            config.set("stocks.$id.totalVolume", stock.totalVolume)
        }
        config.save(File(dataFolder, "stocks.yml"))
    }

    fun load(dataFolder: File) {
        val file = File(dataFolder, "stocks.yml")
        if (!file.exists()) return

        val config = YamlConfiguration.loadConfiguration(file)
        val stocksSection = config.getConfigurationSection("stocks") ?: return

        for (id in stocksSection.getKeys(false)) {
            val section = stocksSection.getConfigurationSection(id) ?: continue
            val existing = stocks[id]
            if (existing != null) {
                // config.yml에서 등록된 종목의 런타임 데이터 복원
                existing.currentPrice = section.getDouble("currentPrice", existing.basePrice)
                existing.totalVolume = section.getLong("totalVolume")
            }
        }
    }
}
