package io.clorose.wolfOfMinestreet.model

import java.util.UUID

data class Stock(
    val id: String,
    val name: String,
    val symbol: String,
    var currentPrice: Double,
    val basePrice: Double,
    var totalVolume: Long = 0
)
