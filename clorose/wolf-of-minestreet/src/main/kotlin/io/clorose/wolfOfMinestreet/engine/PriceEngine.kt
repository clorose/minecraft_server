package io.clorose.wolfOfMinestreet.engine

import io.clorose.wolfOfMinestreet.manager.StockManager
import io.clorose.wolfOfMinestreet.model.*
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.util.ArrayDeque
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.max

class PriceEngine(
    private val plugin: JavaPlugin,
    private val stockManager: StockManager,
    private val saveAction: () -> Unit,
    private val tradeRecorder: TradeRecorder
) {

    lateinit var newsManager: NewsManager
        private set

    private var eventRegistry: EventRegistry? = null

    // 종목별 가격 히스토리 (최근 20틱)
    private val priceHistory = mutableMapOf<String, ArrayDeque<Double>>()

    // VI 정지 상태: stockId -> 남은 틱 수
    private val viFrozen = mutableMapOf<String, Int>()
    // VI 해제 후 쿨다운: stockId -> 남은 틱 수
    private val viCooldown = mutableMapOf<String, Int>()

    // 업종별 뉴스 쿨다운: category -> 남은 틱 수
    private val categoryCooldown = mutableMapOf<String, Int>()
    // 시장 전체 이벤트 쿨다운
    private var marketCooldown: Int = 0
    // 종목별 이벤트 쿨다운
    private val companyCooldown = mutableMapOf<String, Int>()

    // 진행 중인 이벤트: key -> ActiveEvent
    private val activeEvents = mutableMapOf<String, ActiveEvent>()

    // 대기 중인 후속 기사
    private val pendingFollowUps = mutableListOf<PendingFollowUp>()

    var coinManager: CoinManager? = null

    private var taskId: Int = -1
    private var tickCount: Long = 0

    companion object {
        const val TICK_INTERVAL = 200L
        const val VI_COOLDOWN_TICKS = 5
        const val HISTORY_SIZE = 120   // MA120 = 20분 (10초 × 120틱)
        const val SAVE_INTERVAL = 6
        const val MARKET_COOLDOWN = 60     // 시장 이벤트 쿨다운 10분
        const val COMPANY_COOLDOWN = 18    // 종목 이벤트 쿨다운 3분

        // 글로벌 이벤트 시스템
        const val GLOBAL_EVENT_CHANCE = 0.08  // 8%/틱 ≈ 분당 0.5회 트리거
        const val MAX_SUB_EVENTS = 3

        // 코인 하락 드리프트 (누름목)
        const val COIN_DRIFT = -0.0004      // COIN: ~15%/시간 하락 압력
        const val MEMECOIN_DRIFT = -0.001    // MEMECOIN: ~30%/시간 하락 압력

        // 통합 캡 (안전망): 틱당 절대 상한
        const val SAFETY_CAP = 0.20          // ±20%/틱 — 정상 플레이에 영향 없음

    }

    // 주식 랜덤 이벤트 헤드라인
    private val stockDumpHeadlines = listOf(
        "실적 전망 하향 조정", "대주주 지분 매각 공시", "경쟁사 약진에 시장 우려",
        "내부자 대량 매도 포착", "신용등급 하향 검토"
    )
    private val stockPumpHeadlines = listOf(
        "어닝 서프라이즈 발표", "기관 대량 매수세 유입", "대형 신규 계약 체결",
        "정책 수혜주로 부각", "자사주 매입 공시"
    )

    data class ActiveEvent(
        val key: String,
        val tier: EventTier,
        val bullish: Boolean,
        val totalJump: Double,
        var remainingTicks: Int,
        var deliveredJump: Double = 0.0,
        val scope: EventScope = EventScope.CATEGORY,
        val category: String? = null,
        val stockId: String? = null
    )

    enum class EventScope { MARKET, CATEGORY, COMPANY }

    data class EventTickJump(val jump: Double, val tier: EventTier)

    fun start(newsManager: NewsManager, eventRegistry: EventRegistry? = null) {
        this.newsManager = newsManager
        this.eventRegistry = eventRegistry

        stockManager.getAllStocks().forEach { stock ->
            val deque = ArrayDeque<Double>(HISTORY_SIZE)
            deque.addLast(stock.currentPrice)
            priceHistory[stock.id] = deque
        }

        taskId = Bukkit.getScheduler().runTaskTimer(plugin, Runnable { tick() }, TICK_INTERVAL, TICK_INTERVAL).taskId
        val mode = if (eventRegistry != null) "3단계 이벤트" else "기본"
        plugin.logger.info("PriceEngine 시작 (${TICK_INTERVAL / 20}초, $mode)")
    }

    fun stop() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId)
            taskId = -1
            plugin.logger.info("PriceEngine 중지")
        }
    }

    fun isStockFrozen(stockId: String): Boolean {
        return (viFrozen[stockId] ?: 0) > 0
    }

    private fun tick() {
        tickCount++
        val rng = ThreadLocalRandom.current()

        // 쿨다운 감소
        decrementCooldowns()

        // 후속 기사 처리
        processFollowUps(rng)

        // 이벤트 발생 체크
        val registry = eventRegistry
        if (registry != null) {
            rollMarketEvents(rng, registry)
            rollCategoryEvents(rng, registry)
            rollCompanyEvents(rng, registry)
        } else {
            rollCategoryEventsFallback(rng)
        }

        // 진행 중인 이벤트에서 이번 틱 점프 계산
        val tickJumps = computeEventTickJumps(rng)

        // 종목별 가격 업데이트 (코인은 CoinManager가 이벤트 처리, 여기선 GBM만)
        stockManager.getAllStocks().forEach { stock ->
            // VI 정지 처리 (코인은 VI 없음)
            if (stock.grade.viEnabled) {
                val frozen = viFrozen[stock.id]
                if (frozen != null && frozen > 0) {
                    viFrozen[stock.id] = frozen - 1
                    if (frozen - 1 <= 0) {
                        viFrozen.remove(stock.id)
                        viCooldown[stock.id] = VI_COOLDOWN_TICKS
                        Bukkit.broadcastMessage("§e[WOM] §f${stock.name}(${stock.symbol}) 거래 재개")
                    }
                    return@forEach
                }
            }

            val history = priceHistory.getOrPut(stock.id) {
                ArrayDeque<Double>(HISTORY_SIZE).also { it.addLast(stock.currentPrice) }
            }

            val ma = if (history.isEmpty()) stock.currentPrice else history.average()

            // 이 종목에 적용되는 이벤트 점프 합산
            val eventJump = resolveJumpsForStock(stock, tickJumps)

            val r = computeReturn(stock, ma, eventJump, rng)
            val pNext = max(stock.currentPrice * exp(r), stock.grade.floorPrice)
            stockManager.updatePrice(stock.id, pNext)
            tradeRecorder.updatePrice(stock.id, pNext)

            if (history.size >= HISTORY_SIZE) history.removeFirst()
            history.addLast(pNext)

            checkVI(stock, pNext, ma)
        }

        // 글로벌 이벤트 롤 (주식 + 코인 공통)
        rollGlobalEvent(rng)

        // 코인 생명주기 (청산 + 상폐 + 교체)
        coinManager?.tick(rng)

        if (tickCount % SAVE_INTERVAL == 0L) {
            saveAction()
        }
    }

    private fun decrementCooldowns() {
        fun <K> decrement(map: MutableMap<K, Int>) {
            val iter = map.iterator()
            while (iter.hasNext()) {
                val entry = iter.next()
                entry.setValue(entry.value - 1)
                if (entry.value <= 0) iter.remove()
            }
        }
        decrement(categoryCooldown)
        decrement(viCooldown)
        decrement(companyCooldown)
        if (marketCooldown > 0) marketCooldown--
    }

    // === 3단계 이벤트 ===

    private fun rollMarketEvents(rng: ThreadLocalRandom, registry: EventRegistry) {
        if (marketCooldown > 0) return

        for (event in registry.marketEvents) {
            if (rng.nextDouble() >= event.probability) continue

            val bullish = event.bullish ?: rng.nextBoolean()
            val result = resolveStoryEvent(event, bullish, rng)

            val tier = result.tier
            val signedJump = if (result.bullish) result.jump else -result.jump

            val active = ActiveEvent(
                key = "market_${tickCount}",
                tier = tier,
                bullish = result.bullish,
                totalJump = signedJump,
                remainingTicks = tier.durationTicks,
                scope = EventScope.MARKET
            )
            activeEvents[active.key] = active
            marketCooldown = MARKET_COOLDOWN

            newsManager.addNews("시장", result.bullish, abs(result.jump), tier, result.headline)

            val arrow = if (result.bullish) "§a▲" else "§c▼"
            val pct = String.format("%.1f", abs(result.jump) * 100)
            Bukkit.broadcastMessage("§4§l[WOM ${tier.label}§4§l] §f시장 전체 $arrow ${pct}% — ${result.headline}")

            if (result.followUp != null) {
                pendingFollowUps.add(PendingFollowUp(null, null, result.followUp, result.followUp.delayTicks))
            }
            break // 시장 이벤트는 한 번에 하나만
        }
    }

    private fun rollCategoryEvents(rng: ThreadLocalRandom, registry: EventRegistry) {
        for ((category, events) in registry.categoryEvents) {
            if (categoryCooldown.containsKey(category)) continue

            for (event in events) {
                if (rng.nextDouble() >= event.probability) continue

                val bullish = event.bullish ?: rng.nextBoolean()
                val result = resolveStoryEvent(event, bullish, rng)
                val tier = result.tier
                val signedJump = if (result.bullish) result.jump else -result.jump

                val active = ActiveEvent(
                    key = "cat_${category}_${tickCount}",
                    tier = tier, bullish = result.bullish,
                    totalJump = signedJump,
                    remainingTicks = tier.durationTicks,
                    scope = EventScope.CATEGORY,
                    category = category
                )
                activeEvents[active.key] = active
                categoryCooldown[category] = tier.cooldownTicks

                newsManager.addNews(category, result.bullish, abs(result.jump), tier, result.headline)

                val arrow = if (result.bullish) "§a▲" else "§c▼"
                val pct = String.format("%.1f", abs(result.jump) * 100)
                Bukkit.broadcastMessage("§e[WOM ${tier.label}§e] §f${category} 업종 $arrow ${pct}% — ${result.headline}")

                if (result.followUp != null) {
                    pendingFollowUps.add(PendingFollowUp(null, category, result.followUp, result.followUp.delayTicks))
                }
                break // 업종당 하나만
            }
        }
    }

    private fun rollCompanyEvents(rng: ThreadLocalRandom, registry: EventRegistry) {
        for ((stockId, events) in registry.companyEvents) {
            if (companyCooldown.containsKey(stockId)) continue

            val stock = stockManager.getStock(stockId) ?: continue

            for (event in events) {
                if (rng.nextDouble() >= event.probability) continue

                val bullish = event.bullish ?: rng.nextBoolean()
                val result = resolveStoryEvent(event, bullish, rng)
                val tier = result.tier
                val signedJump = if (result.bullish) result.jump else -result.jump

                val active = ActiveEvent(
                    key = "co_${stockId}_${tickCount}",
                    tier = tier, bullish = result.bullish,
                    totalJump = signedJump,
                    remainingTicks = tier.durationTicks,
                    scope = EventScope.COMPANY,
                    stockId = stockId
                )
                activeEvents[active.key] = active
                companyCooldown[stockId] = COMPANY_COOLDOWN

                newsManager.addNews(stock.category, result.bullish, abs(result.jump), tier,
                    "${stock.name}: ${result.headline}")

                val arrow = if (result.bullish) "§a▲" else "§c▼"
                val pct = String.format("%.1f", abs(result.jump) * 100)
                Bukkit.broadcastMessage("§e[WOM ${tier.label}§e] §f${stock.name}(${stock.symbol}) $arrow ${pct}% — ${result.headline}")

                if (result.followUp != null) {
                    pendingFollowUps.add(PendingFollowUp(stockId, stock.category, result.followUp, result.followUp.delayTicks))
                }
                break
            }
        }
    }

    /** 기존 폴백: registry 없을 때 하드코딩 이벤트 */
    private fun rollCategoryEventsFallback(rng: ThreadLocalRandom) {
        val categories = stockManager.getAllStocks().map { it.category }.distinct()
        for (cat in categories) {
            if (categoryCooldown.containsKey(cat)) continue
            if (activeEvents.any { it.value.category == cat }) continue

            val tier = EventTier.roll() ?: continue
            val bullish = rng.nextBoolean()
            val totalJump = rng.nextDouble(tier.jumpMin, tier.jumpMax)
            val signedJump = if (bullish) totalJump else -totalJump

            val active = ActiveEvent(
                key = "cat_${cat}_${tickCount}",
                tier = tier, bullish = bullish,
                totalJump = signedJump,
                remainingTicks = tier.durationTicks,
                scope = EventScope.CATEGORY,
                category = cat
            )
            activeEvents[active.key] = active
            categoryCooldown[cat] = tier.cooldownTicks

            newsManager.addNews(cat, bullish, totalJump, tier)

            val arrow = if (bullish) "§a▲" else "§c▼"
            val pct = String.format("%.1f", totalJump * 100)
            Bukkit.broadcastMessage("§e[WOM ${tier.label}§e] §f${cat} 업종 $arrow ${pct}% 충격!")
        }
    }

    data class ResolvedEvent(
        val headline: String,
        val bullish: Boolean,
        val jump: Double,
        val tier: EventTier,
        val followUp: StoryFollowUp?
    )

    private fun resolveStoryEvent(event: StoryEvent, bullish: Boolean, rng: ThreadLocalRandom): ResolvedEvent {
        // 분기형 이벤트
        if (event.successRate != null && event.success != null && event.failure != null) {
            val success = rng.nextDouble() < event.successRate
            val outcome = if (success) event.success else event.failure
            return ResolvedEvent(
                headline = outcome.headline,
                bullish = outcome.jump > 0,
                jump = abs(outcome.jump),
                tier = outcome.tier,
                followUp = outcome.followUp
            )
        }

        // 단순 이벤트
        val jump = rng.nextDouble(event.jumpMin, event.jumpMax)
        return ResolvedEvent(
            headline = event.headline,
            bullish = bullish,
            jump = jump,
            tier = event.tier,
            followUp = null
        )
    }

    private fun processFollowUps(rng: ThreadLocalRandom) {
        val iter = pendingFollowUps.iterator()
        while (iter.hasNext()) {
            val pfu = iter.next()
            pfu.remainingTicks--
            if (pfu.remainingTicks > 0) continue

            iter.remove()
            val fu = pfu.followUp
            val bullish = fu.jump > 0
            val signedJump = fu.jump

            val scope = when {
                pfu.stockId != null -> EventScope.COMPANY
                pfu.category != null -> EventScope.CATEGORY
                else -> EventScope.MARKET
            }

            val active = ActiveEvent(
                key = "fu_${tickCount}_${rng.nextInt(1000)}",
                tier = fu.tier, bullish = bullish,
                totalJump = signedJump,
                remainingTicks = 1,
                scope = scope,
                category = pfu.category,
                stockId = pfu.stockId
            )
            activeEvents[active.key] = active

            val label = when {
                pfu.stockId != null -> stockManager.getStock(pfu.stockId)?.name ?: pfu.stockId
                pfu.category != null -> "${pfu.category} 업종"
                else -> "시장"
            }

            newsManager.addNews(pfu.category ?: "시장", bullish, abs(fu.jump), fu.tier, fu.headline)

            val arrow = if (bullish) "§a▲" else "§c▼"
            val pct = String.format("%.1f", abs(fu.jump) * 100)
            Bukkit.broadcastMessage("§e[WOM 후속] §f${label} $arrow ${pct}% — ${fu.headline}")
        }
    }

    private fun computeEventTickJumps(rng: ThreadLocalRandom): Map<String, EventTickJump> {
        val result = mutableMapOf<String, EventTickJump>()

        val iter = activeEvents.iterator()
        while (iter.hasNext()) {
            val (key, event) = iter.next()

            val remaining = event.totalJump - event.deliveredJump
            // 프론트로드 곡선: 초반 충격 → 점진 수렴 (2/(r+1) 가중치)
            val base = remaining * 2.0 / (event.remainingTicks + 1)
            val noise = base * rng.nextDouble(-0.2, 0.2)
            val tickJump = base + noise

            event.deliveredJump += tickJump
            event.remainingTicks--

            result[key] = EventTickJump(tickJump, event.tier)

            if (event.remainingTicks <= 0) iter.remove()
        }

        return result
    }

    /** 종목에 적용되는 이벤트 점프 합산 (시장 + 업종 + 종목 전용) */
    private fun resolveJumpsForStock(stock: Stock, tickJumps: Map<String, EventTickJump>): EventTickJump? {
        var totalJump = 0.0
        var maxTier = EventTier.NORMAL
        val noCap = stock.grade.isCoin  // 코인은 이벤트 캡 없음

        for ((key, jump) in tickJumps) {
            val event = activeEvents[key] ?: run {
                val applies = when {
                    key.startsWith("market_") || key.startsWith("fu_") -> true
                    else -> false
                }
                if (applies) {
                    totalJump += if (noCap) jump.jump else jump.jump.coerceIn(-jump.tier.capEvent, jump.tier.capEvent)
                    if (jump.tier.ordinal > maxTier.ordinal) maxTier = jump.tier
                }
                continue
            }

            val applies = when (event.scope) {
                EventScope.MARKET -> true
                EventScope.CATEGORY -> event.category == stock.category
                EventScope.COMPANY -> event.stockId == stock.id
            }

            if (applies) {
                totalJump += if (noCap) jump.jump else jump.jump.coerceIn(-jump.tier.capEvent, jump.tier.capEvent)
                if (jump.tier.ordinal > maxTier.ordinal) maxTier = jump.tier
            }
        }

        return if (totalJump != 0.0) EventTickJump(totalJump, maxTier) else null
    }

    private fun computeReturn(stock: Stock, ma: Double, eventJump: EventTickJump?, rng: ThreadLocalRandom): Double {
        val grade = stock.grade
        val z = rng.nextGaussian()
        val v = stock.volatility

        // 코인 누름목: 지속적 하락 드리프트
        val mu = when (grade) {
            AssetGrade.MEMECOIN -> MEMECOIN_DRIFT
            AssetGrade.COIN -> COIN_DRIFT
            else -> 0.0
        }
        val rGbm = mu - v * v / 2.0 + v * z  // 이토 보정 + 코인 드리프트

        val rMr = if (grade.mrStrength > 0 && ma > 0 && stock.currentPrice > 0) {
            grade.mrStrength * ln(ma / stock.currentPrice)
        } else 0.0

        val rBase = (rGbm + rMr).coerceIn(-grade.capBase, grade.capBase)

        val rEvent = eventJump?.jump ?: 0.0  // 이미 capEvent 적용됨

        // 통합 캡 (안전망): 틱당 최대 ±20% — 버그/이벤트 중첩 폭주 방지
        return (rBase + rEvent).coerceIn(-SAFETY_CAP, SAFETY_CAP)
    }

    // === 글로벌 이벤트 시스템 ===

    /** 틱당 1회 굴림 → 트리거 시 1~N개 하위 이벤트 발생 */
    private fun rollGlobalEvent(rng: ThreadLocalRandom) {
        if (rng.nextDouble() >= GLOBAL_EVENT_CHANCE) return

        // 하위 이벤트 개수: 1=50%, 2=35%, 3=15%
        val roll = rng.nextDouble()
        val numEvents = when {
            roll < 0.50 -> 1
            roll < 0.85 -> 2
            else -> MAX_SUB_EVENTS
        }

        val allStocks = stockManager.getAllStocks()
        if (allStocks.isEmpty()) return

        // 중복 대상 방지
        val targets = mutableSetOf<String>()
        repeat(numEvents) {
            val target = allStocks[rng.nextInt(allStocks.size)]
            if (target.id !in targets) {
                targets.add(target.id)
                if (target.grade.isCoin) {
                    coinManager?.applyRandomEvent(target, rng)
                } else {
                    applyStockRandomEvent(target, rng)
                }
            }
        }
    }

    /** 주식 랜덤 이벤트: 60% 하락 / 40% 상승, ±0.5~1.5% */
    private fun applyStockRandomEvent(stock: Stock, rng: ThreadLocalRandom) {
        val bullish = rng.nextDouble() < 0.4
        val jump = rng.nextDouble(0.005, 0.015)
        val signedJump = if (bullish) jump else -jump

        val headline = if (bullish) stockPumpHeadlines.random() else stockDumpHeadlines.random()

        val active = ActiveEvent(
            key = "random_${stock.id}_${tickCount}",
            tier = EventTier.NORMAL,
            bullish = bullish,
            totalJump = signedJump,
            remainingTicks = 3,  // 30초에 걸쳐 반영
            scope = EventScope.COMPANY,
            stockId = stock.id
        )
        activeEvents[active.key] = active

        newsManager.addNews(stock.category, bullish, jump, EventTier.NORMAL,
            "${stock.name}: $headline")

        val arrow = if (bullish) "§a▲" else "§c▼"
        val pct = String.format("%.1f", jump * 100)
        Bukkit.broadcastMessage("§e[WOM] §f${stock.name}(${stock.symbol}) $arrow ${pct}% — $headline")
    }

    private fun checkVI(stock: Stock, pNext: Double, ma: Double) {
        if (!stock.grade.viEnabled) return
        if (ma <= 0) return
        if (viCooldown.containsKey(stock.id)) return

        val deviation = (pNext - ma) / ma

        if (abs(deviation) > stock.grade.viThreshold()) {
            val event = activeEvents.values.find {
                (it.scope == EventScope.COMPANY && it.stockId == stock.id) ||
                (it.scope == EventScope.CATEGORY && it.category == stock.category) ||
                it.scope == EventScope.MARKET
            }
            val freezeTicks = event?.tier?.viFreezeTicks ?: EventTier.NORMAL.viFreezeTicks

            viFrozen[stock.id] = freezeTicks

            val direction = if (deviation > 0) "상승" else "하락"
            val pctStr = String.format("%.1f", deviation * 100)

            newsManager.addVINews(stock, direction)
            Bukkit.broadcastMessage("§c§l[VI 발동] §f${stock.name}(${stock.symbol}) ${direction} ${pctStr}% — ${freezeTicks * 10}초 거래정지")
        }
    }
}
