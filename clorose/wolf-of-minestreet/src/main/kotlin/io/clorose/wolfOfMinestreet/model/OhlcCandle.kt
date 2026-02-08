package io.clorose.wolfOfMinestreet.model

data class OhlcCandle(
    val time: Long,   // epoch seconds (lightweight-charts expects seconds, not millis)
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Long
)
