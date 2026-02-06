package io.clorose.wolfOfMinestreet.manager

import io.clorose.wolfOfMinestreet.model.Portfolio
import java.util.UUID

class PortfolioManager {
    private val portfolios = mutableMapOf<UUID, Portfolio>()

    fun getPortfolio(playerUUID: UUID): Portfolio {
        return portfolios.getOrPut(playerUUID) { Portfolio(playerUUID) }
    }

    fun buyStock(playerUUID: UUID, stockId: String, amount: Long) {
        val portfolio = getPortfolio(playerUUID)
        portfolio.addStock(stockId, amount)
    }

    fun sellStock(playerUUID: UUID, stockId: String, amount: Long): Boolean {
        val portfolio = getPortfolio(playerUUID)
        return portfolio.removeStock(stockId, amount)
    }
}
