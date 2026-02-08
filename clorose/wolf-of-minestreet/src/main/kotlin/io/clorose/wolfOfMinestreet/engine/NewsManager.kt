package io.clorose.wolfOfMinestreet.engine

import io.clorose.wolfOfMinestreet.model.EventTier
import io.clorose.wolfOfMinestreet.model.NewsItem
import io.clorose.wolfOfMinestreet.model.Stock
import java.util.ArrayDeque

class NewsManager {

    private val news = ArrayDeque<NewsItem>(50)

    private val normalBullish = listOf(
        "%s 업종, 정부 지원책 발표로 상승",
        "%s 업종, 호실적 발표에 투자자 환호",
        "%s 업종, 신규 계약 체결 소식",
        "%s 업종, 해외 수출 증가 기대감",
        "%s 업종, 규제 완화 소식에 상승세"
    )
    private val normalBearish = listOf(
        "%s 업종, 실적 부진 우려로 하락",
        "%s 업종, 규제 강화 소식에 약세",
        "%s 업종, 원자재 가격 상승 우려",
        "%s 업종, 리콜 사태 발생",
        "%s 업종, 글로벌 수요 감소 전망"
    )

    private val majorBullish = listOf(
        "%s 업종, 대규모 정부 투자 확정! 시장 흥분",
        "%s 업종, 혁신 기술 상용화 성공 — 폭발적 성장 기대",
        "%s 업종, 글로벌 대기업 인수합병 소식에 급등"
    )
    private val majorBearish = listOf(
        "%s 업종, 대형 기업 파산 위기설 — 시장 패닉",
        "%s 업종, 정부 긴급 규제 발표에 급락",
        "%s 업종, 공급망 붕괴 우려 확산"
    )

    private val extremeBullish = listOf(
        "§6§l[초대형 호재] %s 업종, 역사적 신기록! 투자자 광풍",
        "§6§l[초대형 호재] %s 업종, 전례 없는 호황 — 밈주 열풍!"
    )
    private val extremeBearish = listOf(
        "§4§l[초대형 악재] %s 업종, 블랙스완 — 시장 대폭락!",
        "§4§l[초대형 악재] %s 업종, 역대급 위기 — 투매 공포!"
    )

    fun addNews(category: String, bullish: Boolean, jumpSize: Double, tier: EventTier, customHeadline: String? = null) {
        val headline = if (customHeadline != null) {
            customHeadline
        } else {
            val templates = when (tier) {
                EventTier.NORMAL -> if (bullish) normalBullish else normalBearish
                EventTier.MAJOR -> if (bullish) majorBullish else majorBearish
                EventTier.EXTREME -> if (bullish) extremeBullish else extremeBearish
            }
            templates.random().format(category)
        }
        val item = NewsItem(
            timestamp = System.currentTimeMillis(),
            headline = headline,
            category = category,
            bullish = bullish,
            impactPercent = jumpSize * 100,
            tier = tier
        )
        synchronized(news) {
            if (news.size >= 50) news.removeLast()
            news.addFirst(item)
        }
    }

    fun addVINews(stock: Stock, direction: String) {
        val headline = "§c[VI 발동] §f${stock.name}(${stock.symbol}) ${direction} 변동성 완화장치 발동"
        val item = NewsItem(
            timestamp = System.currentTimeMillis(),
            headline = headline,
            category = stock.category,
            bullish = false,
            impactPercent = 0.0
        )
        synchronized(news) {
            if (news.size >= 50) news.removeLast()
            news.addFirst(item)
        }
    }

    fun getRecentNews(count: Int): List<NewsItem> {
        synchronized(news) {
            return news.take(count)
        }
    }
}
