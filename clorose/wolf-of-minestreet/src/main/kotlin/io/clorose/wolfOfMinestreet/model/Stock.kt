package io.clorose.wolfOfMinestreet.model

import java.util.UUID

data class Stock(
    val id: String,
    val name: String,
    val symbol: String,
    var currentPrice: Double,
    val basePrice: Double,
    val category: String = "기타",
    val volatility: Double = 0.05,
    var totalVolume: Long = 0,
    val grade: AssetGrade = AssetGrade.GROWTH,
    val totalShares: Long = 10_000
) {
    val marketCap: Double get() = currentPrice * totalShares
}
