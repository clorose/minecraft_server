package io.clorose.wolfOfMinestreet.model

/**
 * YAML에서 로드되는 이벤트 템플릿.
 * 3단계: market(시장 전체), category(업종 공통), company(종목 전용)
 */
data class StoryEvent(
    val headline: String,
    val tier: EventTier = EventTier.NORMAL,
    val bullish: Boolean? = null,         // null이면 랜덤
    val jumpMin: Double = 0.005,
    val jumpMax: Double = 0.03,
    val probability: Double = 0.03,       // 발동 확률
    val successRate: Double? = null,       // 분기형 이벤트: 성공 확률
    val success: StoryOutcome? = null,
    val failure: StoryOutcome? = null
)

data class StoryOutcome(
    val headline: String,
    val jump: Double,
    val tier: EventTier = EventTier.NORMAL,
    val followUp: StoryFollowUp? = null
)

data class StoryFollowUp(
    val delayTicks: Int = 3,
    val headline: String,
    val jump: Double,
    val tier: EventTier = EventTier.NORMAL
)

/**
 * 진행 중인 체인 이벤트 (후속 기사 대기)
 */
data class PendingFollowUp(
    val stockId: String?,       // null이면 업종/시장 전체
    val category: String?,
    val followUp: StoryFollowUp,
    var remainingTicks: Int
)

/**
 * 이벤트 저장소: 3단계 분리
 */
data class EventRegistry(
    val marketEvents: MutableList<StoryEvent> = mutableListOf(),
    val categoryEvents: MutableMap<String, MutableList<StoryEvent>> = mutableMapOf(),
    val companyEvents: MutableMap<String, MutableList<StoryEvent>> = mutableMapOf()
)
