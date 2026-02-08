package io.clorose.wolfOfMinestreet.model

enum class AssetGrade(
    val displayName: String,
    val colorCode: String,
    val capBase: Double,
    val mrStrength: Double,
    val viEnabled: Boolean,
    val floorPrice: Double,
    val liquidation: Boolean,
    val isCoin: Boolean
) {
    BLUECHIP("우량주", "§b", 0.002, 0.015, true, 1.0, false, false),
    GROWTH("성장주", "§a", 0.003, 0.01, true, 1.0, false, false),
    SPECULATIVE("투기주", "§e", 0.005, 0.005, true, 1.0, false, false),
    COIN("코인", "§d", 999.0, 0.0, false, 0.01, true, true),
    MEMECOIN("밈코인", "§5", 999.0, 0.0, false, 0.01, true, true);

    fun viThreshold(): Double = when (this) {
        BLUECHIP, GROWTH -> 0.08
        SPECULATIVE -> 0.10
        else -> 999.0
    }
}
