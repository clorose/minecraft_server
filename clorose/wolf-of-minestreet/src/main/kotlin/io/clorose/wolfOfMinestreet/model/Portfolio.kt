package io.clorose.wolfOfMinestreet.model

import java.util.UUID

data class Portfolio(
    val playerUUID: UUID,
    val holdings: MutableMap<String, Long> = mutableMapOf()
) {
    fun addStock(stockId: String, amount: Long) {
        holdings[stockId] = holdings.getOrDefault(stockId, 0) + amount
    }

    fun removeStock(stockId: String, amount: Long): Boolean {
        val current = holdings.getOrDefault(stockId, 0)
        if (current < amount) return false

        val newAmount = current - amount
        if (newAmount == 0L) {
            holdings.remove(stockId)
        } else {
            holdings[stockId] = newAmount
        }
        return true
    }

    fun getAmount(stockId: String): Long {
        return holdings.getOrDefault(stockId, 0)
    }
}
