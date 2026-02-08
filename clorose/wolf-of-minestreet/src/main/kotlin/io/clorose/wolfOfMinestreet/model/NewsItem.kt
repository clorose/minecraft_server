package io.clorose.wolfOfMinestreet.model

data class NewsItem(
    val timestamp: Long,
    val headline: String,
    val category: String,
    val bullish: Boolean,
    val impactPercent: Double,
    val tier: EventTier = EventTier.NORMAL
)
