package io.clorose.wolfOfMinestreet.model

enum class TradeSide { BUY, SELL }

data class TradeRecord(
    val timestamp: Long,
    val playerName: String,
    val symbol: String,
    val stockId: String,
    val side: TradeSide,
    val quantity: Long,
    val price: Double
)
