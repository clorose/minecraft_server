package io.clorose.wolfOfMinestreet.model

enum class EventTier(
    val label: String,
    val capEvent: Double,
    val durationTicks: Int,
    val cooldownTicks: Int,
    val viFreezeTicks: Int,
    val jumpMin: Double,
    val jumpMax: Double
) {
    NORMAL("속보", 0.03, 1, 30, 3, 0.005, 0.03),
    MAJOR("§6긴급", 0.08, 6, 60, 8, 0.03, 0.08),
    EXTREME("§c§l초대형", 0.15, 12, 240, 15, 0.08, 0.15);

    companion object {
        fun roll(): EventTier? {
            val roll = Math.random()
            return when {
                roll < 0.001 -> EXTREME   // 0.1%
                roll < 0.008 -> MAJOR     // 0.7%
                roll < 0.03  -> NORMAL    // 2.2%
                else -> null
            }
        }
    }
}
