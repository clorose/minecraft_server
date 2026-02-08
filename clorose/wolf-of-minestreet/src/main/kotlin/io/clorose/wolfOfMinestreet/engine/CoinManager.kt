package io.clorose.wolfOfMinestreet.engine

import io.clorose.wolfOfMinestreet.manager.PortfolioManager
import io.clorose.wolfOfMinestreet.manager.StockManager
import io.clorose.wolfOfMinestreet.model.AssetGrade
import io.clorose.wolfOfMinestreet.model.EventTier
import io.clorose.wolfOfMinestreet.model.Stock
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.util.concurrent.ThreadLocalRandom

class CoinManager(
    private val plugin: JavaPlugin,
    private val stockManager: StockManager,
    private val portfolioManager: PortfolioManager,
    private val newsManager: NewsManager,
    private val economy: Economy?,
    private val tradeRecorder: TradeRecorder
) {

    companion object {
        const val TARGET_COINS = 3
        const val TARGET_MEMECOINS = 2
        const val TOTAL_COINS = TARGET_COINS + TARGET_MEMECOINS
        const val REPLACEMENT_DELAY_TICKS = 30  // 5분 (30 × 10초)
        const val LISTING_PUMP_DURATION = 18    // 3분 (18 × 10초)
        const val LIQUIDATION_THRESHOLD = 0.5   // 평균매수가 대비 50%
        const val DELIST_PRICE = 0.01

        val PREFIXES = listOf(
            "문", "도지", "페페", "시바", "엘론", "로켓", "다이아", "골드", "실버",
            "네더", "엔더", "크리퍼", "위더", "드래곤", "피닉스", "슬라임", "블레이즈",
            "팬텀", "좀비", "해골", "가스트", "셜커", "철골", "레드스톤", "옵시디언"
        )
        val SUFFIXES = listOf("코인", "토큰", "캐시", "체인", "스왑", "파이", "문", "스타")

        // 코인 이벤트 가중치 — PriceEngine 글로벌 롤에서 코인 선택 시 사용
        // 기본: 러그 5% + 문샷 5% + 펌프 20% + 덤프 70%
        // 부스트: 러그 5% + 문샷 10% + 펌프 30% + 덤프 55%
        const val RUG_WEIGHT = 0.05
        const val RUG_WEIGHT_MEME = 0.10
        const val MOON_WEIGHT = 0.05
        const val MOON_WEIGHT_BOOSTED = 0.10
        const val PUMP_WEIGHT = 0.20
        const val PUMP_WEIGHT_BOOSTED = 0.30
    }

    // 활성 코인 ID 목록
    private val activeCoins = mutableSetOf<String>()
    // 상장 직후 펌프 부스트: coinId -> 남은 틱
    private val listingBoost = mutableMapOf<String, Int>()
    // 교체 대기: 남은 틱 -> 등급
    private val pendingReplacements = mutableListOf<PendingReplacement>()
    // 이미 사용된 이름 (중복 방지)
    private val usedNames = mutableSetOf<String>()

    data class PendingReplacement(val grade: AssetGrade, var remainingTicks: Int)

    private val dumpHeadlines = listOf(
        "개발자 물량 대량 매도",
        "보안 취약점 발견",
        "규제 당국 조사 착수",
        "고래 지갑 대량 매도 포착",
        "텔레그램 커뮤니티 와해",
        "거래소 지갑 해킹 의혹",
        "경쟁 코인에 기술력 밀려"
    )
    private val pumpHeadlines = listOf(
        "유명 인플루언서 추천!",
        "대형 거래소 상장 루머",
        "기술 업데이트 발표",
        "고래 지갑 대량 매집 포착",
        "유명 기업 결제 수단 채택설"
    )
    private val moonHeadlines = listOf(
        "나유타 시스템즈 공식 채택!",
        "정부 디지털 화폐 기반 기술 선정",
        "글로벌 결제망 편입 확정"
    )
    private val rugHeadlines = listOf(
        "개발팀 잠적 — 러그풀 확정",
        "스마트 컨트랙트 백도어 발견"
    )

    /** 초기화: 코인 5개 채우기 */
    fun initialize() {
        val currentCoins = stockManager.getAllStocks().filter { it.grade.isCoin }
        currentCoins.forEach { activeCoins.add(it.id) }
        usedNames.addAll(currentCoins.map { it.name })

        val coinCount = currentCoins.count { it.grade == AssetGrade.COIN }
        val memeCount = currentCoins.count { it.grade == AssetGrade.MEMECOIN }

        repeat(TARGET_COINS - coinCount) { listNewCoin(AssetGrade.COIN) }
        repeat(TARGET_MEMECOINS - memeCount) { listNewCoin(AssetGrade.MEMECOIN) }
    }

    /** 매 틱 호출 — 이벤트는 PriceEngine 글로벌 롤에서 처리 */
    fun tick(rng: ThreadLocalRandom) {
        // 교체 대기 처리
        val replaceIter = pendingReplacements.iterator()
        while (replaceIter.hasNext()) {
            val pending = replaceIter.next()
            pending.remainingTicks--
            if (pending.remainingTicks <= 0) {
                replaceIter.remove()
                listNewCoin(pending.grade)
            }
        }

        // 리스팅 부스트 감소
        val boostIter = listingBoost.iterator()
        while (boostIter.hasNext()) {
            val entry = boostIter.next()
            entry.setValue(entry.value - 1)
            if (entry.value <= 0) boostIter.remove()
        }

        // 청산 체크
        checkLiquidations()

        // 상폐 체크
        checkDelistings()
    }

    /** PriceEngine 글로벌 이벤트에서 호출: 코인에 랜덤 이벤트 적용 */
    fun applyRandomEvent(coin: Stock, rng: ThreadLocalRandom) {
        val isMeme = coin.grade == AssetGrade.MEMECOIN
        val boosted = listingBoost.containsKey(coin.id)
        val roll = rng.nextDouble()

        val rugW = if (isMeme) RUG_WEIGHT_MEME else RUG_WEIGHT
        val moonW = if (boosted) MOON_WEIGHT_BOOSTED else MOON_WEIGHT
        val pumpW = if (boosted) PUMP_WEIGHT_BOOSTED else PUMP_WEIGHT
        // 나머지 = 덤프

        when {
            roll < rugW -> applyRugpull(coin)
            roll < rugW + moonW -> applyMoonshot(coin, rng)
            roll < rugW + moonW + pumpW -> applyPump(coin, rng)
            else -> applyDump(coin, rng)
        }
    }

    private fun applyRugpull(coin: Stock) {
        val headline = rugHeadlines.random()
        newsManager.addNews("코인", false, 1.0, EventTier.EXTREME,
            "${coin.name}: $headline")
        Bukkit.broadcastMessage("§4§l[러그풀] §f${coin.name}(${coin.symbol}) $headline")
        stockManager.updatePrice(coin.id, 0.001)
        tradeRecorder.updatePrice(coin.id, 0.001)
    }

    private fun applyMoonshot(coin: Stock, rng: ThreadLocalRandom) {
        val jump = rng.nextDouble(0.50, 2.00)  // +50% ~ +200%
        val headline = moonHeadlines.random()
        val newPrice = coin.currentPrice * (1.0 + jump)
        stockManager.updatePrice(coin.id, newPrice)
        tradeRecorder.updatePrice(coin.id, newPrice)

        val pct = String.format("%.0f", jump * 100)
        newsManager.addNews("코인", true, jump, EventTier.EXTREME,
            "${coin.name}: $headline")
        Bukkit.broadcastMessage("§6§l[문샷] §f${coin.name}(${coin.symbol}) §a▲+${pct}%! $headline")
    }

    private fun applyPump(coin: Stock, rng: ThreadLocalRandom) {
        val jump = rng.nextDouble(0.10, 0.50)  // +10% ~ +50%
        val newPrice = coin.currentPrice * (1.0 + jump)
        stockManager.updatePrice(coin.id, newPrice)
        tradeRecorder.updatePrice(coin.id, newPrice)

        val pct = String.format("%.1f", jump * 100)
        val headline = pumpHeadlines.random()
        newsManager.addNews("코인", true, jump, EventTier.MAJOR,
            "${coin.name}: $headline")
        Bukkit.broadcastMessage("§a[펌프] §f${coin.name}(${coin.symbol}) §a▲+${pct}% $headline")
    }

    private fun applyDump(coin: Stock, rng: ThreadLocalRandom) {
        val jump = rng.nextDouble(0.05, 0.15)  // -5% ~ -15%
        val newPrice = (coin.currentPrice * (1.0 - jump)).coerceAtLeast(coin.grade.floorPrice)
        stockManager.updatePrice(coin.id, newPrice)
        tradeRecorder.updatePrice(coin.id, newPrice)

        val pct = String.format("%.1f", jump * 100)
        val headline = dumpHeadlines.random()
        newsManager.addNews("코인", false, jump, EventTier.MAJOR,
            "${coin.name}: $headline")
        Bukkit.broadcastMessage("§c[덤프] §f${coin.name}(${coin.symbol}) §c▼-${pct}% $headline")
    }

    /** 청산 체크: 평균매수가 -50% → 강제 매도 */
    private fun checkLiquidations() {
        val coins = stockManager.getAllStocks().filter { it.grade.isCoin && activeCoins.contains(it.id) }
        val liquidatedBatch = mutableMapOf<String, Int>()  // coinName -> 청산 인원

        for (coin in coins) {
            val holders = portfolioManager.getHolders(coin.id)
            for ((uuid, amount) in holders) {
                val avgPrice = portfolioManager.getAvgBuyPrice(uuid, coin.id)
                if (avgPrice <= 0) continue

                if (coin.currentPrice <= avgPrice * LIQUIDATION_THRESHOLD + 0.0001) {
                    val liquidatedAmount = portfolioManager.liquidate(uuid, coin.id)
                    if (liquidatedAmount <= 0) continue

                    val payout = coin.currentPrice * liquidatedAmount
                    val player = Bukkit.getPlayer(uuid)

                    economy?.let { econ ->
                        val offlinePlayer = Bukkit.getOfflinePlayer(uuid)
                        econ.depositPlayer(offlinePlayer, payout)
                    }

                    val payoutStr = String.format("%.2f", payout)
                    if (player != null && player.isOnline) {
                        player.sendMessage("§c[청산] ${coin.name} -50% 돌파 — ${liquidatedAmount}개 강제 매도 (${payoutStr}원 지급)")
                    } else {
                        portfolioManager.addPendingNotification(uuid,
                            "§c[청산] 부재 중 ${coin.name}에서 청산당했습니다. (${liquidatedAmount}개 → ${payoutStr}원 지급)")
                    }

                    liquidatedBatch[coin.name] = (liquidatedBatch[coin.name] ?: 0) + 1
                }
            }
        }

        // 대량 청산 배치 공지
        for ((coinName, count) in liquidatedBatch) {
            if (count > 0) {
                Bukkit.broadcastMessage("§c[대량 청산] ${coinName} 폭락 — ${count}명 청산")
            }
        }
    }

    /** 상폐 체크: 가격 ≤ 0.01 */
    private fun checkDelistings() {
        val coins = stockManager.getAllStocks().filter { it.grade.isCoin && activeCoins.contains(it.id) }.toList()

        for (coin in coins) {
            if (coin.currentPrice > DELIST_PRICE + 0.0001) continue

            // 상폐 처리
            val holders = portfolioManager.getHolders(coin.id)
            var totalVictims = 0
            var totalLoss = 0.0

            for ((uuid, amount) in holders) {
                val avgPrice = portfolioManager.getAvgBuyPrice(uuid, coin.id)
                val loss = avgPrice * amount
                totalLoss += loss
                totalVictims++

                portfolioManager.delistForPlayer(uuid, coin.id)

                val player = Bukkit.getPlayer(uuid)
                val lossStr = String.format("%.2f", loss)
                if (player != null && player.isOnline) {
                    player.sendMessage("§4[상장폐지] ${coin.name} 사망 — ${amount}개 전액 손실 (${lossStr}원)")
                } else {
                    portfolioManager.addPendingNotification(uuid,
                        "§4[상장폐지] 부재 중 ${coin.name}이(가) 상장폐지되었습니다. (${amount}개 전액 손실)")
                }
            }

            // 서버 공지
            val totalLossStr = String.format("%.0f", totalLoss)
            Bukkit.broadcastMessage("§4§l[상장폐지] ${coin.name}(${coin.symbol}) 사망! 피해자 ${totalVictims}명, 총 피해 ${totalLossStr}원")

            // 코인 제거 + 교체 예약
            activeCoins.remove(coin.id)
            listingBoost.remove(coin.id)
            stockManager.removeStock(coin.id)
            pendingReplacements.add(PendingReplacement(coin.grade, REPLACEMENT_DELAY_TICKS))

            Bukkit.broadcastMessage("§7${REPLACEMENT_DELAY_TICKS * 10 / 60}분 후 새 코인 상장 예정...")
        }
    }

    /** 새 코인 상장 */
    private fun listNewCoin(grade: AssetGrade) {
        val rng = ThreadLocalRandom.current()
        val name = generateName()
        val symbol = generateSymbol(name)
        val id = "coin_${symbol.lowercase()}_${System.currentTimeMillis() % 100000}"

        val basePrice = when (grade) {
            AssetGrade.MEMECOIN -> rng.nextDouble(500.0, 3000.0)
            else -> rng.nextDouble(2000.0, 10000.0)
        }
        val volatility = when (grade) {
            AssetGrade.MEMECOIN -> rng.nextDouble(0.04, 0.06)
            else -> rng.nextDouble(0.02, 0.04)
        }

        val price = String.format("%.2f", basePrice).toDouble()
        val stock = Stock(id, name, symbol, price, price, "코인", volatility, 0, grade)
        stockManager.registerStock(stock)
        activeCoins.add(id)
        listingBoost[id] = LISTING_PUMP_DURATION

        val gradeLabel = grade.colorCode + grade.displayName
        Bukkit.broadcastMessage("§d§l[신규 상장] §f${name}(${symbol}) 거래 시작! 초기 가격 ${price}원 [$gradeLabel§f]")
        newsManager.addNews("코인", true, 0.0, EventTier.NORMAL,
            "신규 상장: ${name}(${symbol}) — ${grade.displayName}")
    }

    /** 이름 생성 (prefix + suffix, 중복 방지) */
    private fun generateName(): String {
        val rng = ThreadLocalRandom.current()
        for (i in 0 until 100) {
            val name = PREFIXES[rng.nextInt(PREFIXES.size)] + SUFFIXES[rng.nextInt(SUFFIXES.size)]
            if (name !in usedNames) {
                usedNames.add(name)
                return name
            }
        }
        // 풀 소진 시 숫자 붙이기
        val name = PREFIXES.random() + SUFFIXES.random() + rng.nextInt(100)
        usedNames.add(name)
        return name
    }

    /** 심볼 생성 (이름 그대로 사용, 중복 시 숫자 추가) */
    private fun generateSymbol(name: String): String {
        return if (stockManager.getStockBySymbol(name) != null) {
            name + ThreadLocalRandom.current().nextInt(10, 99)
        } else {
            name
        }
    }

    fun getActiveCoins(): List<Stock> {
        return activeCoins.mapNotNull { stockManager.getStock(it) }
    }

    fun isActiveCoin(stockId: String): Boolean = stockId in activeCoins

    // === 저장/로드 ===

    fun save(dataFolder: File) {
        val config = YamlConfiguration()
        val coins = getActiveCoins()
        coins.forEachIndexed { i, coin ->
            config.set("coins.$i.id", coin.id)
            config.set("coins.$i.name", coin.name)
            config.set("coins.$i.symbol", coin.symbol)
            config.set("coins.$i.currentPrice", coin.currentPrice)
            config.set("coins.$i.basePrice", coin.basePrice)
            config.set("coins.$i.volatility", coin.volatility)
            config.set("coins.$i.grade", coin.grade.name)
        }
        config.set("usedNames", usedNames.toList())

        // 교체 대기 저장
        pendingReplacements.forEachIndexed { i, pr ->
            config.set("pending.$i.grade", pr.grade.name)
            config.set("pending.$i.ticks", pr.remainingTicks)
        }

        config.save(File(dataFolder, "active_coins.yml"))
    }

    fun load(dataFolder: File) {
        val file = File(dataFolder, "active_coins.yml")
        if (!file.exists()) return

        val config = YamlConfiguration.loadConfiguration(file)

        val coinsSection = config.getConfigurationSection("coins")
        if (coinsSection != null) {
            for (key in coinsSection.getKeys(false)) {
                val sec = coinsSection.getConfigurationSection(key) ?: continue
                val id = sec.getString("id") ?: continue
                val name = sec.getString("name") ?: continue
                val symbol = sec.getString("symbol") ?: continue
                val currentPrice = sec.getDouble("currentPrice")
                val basePrice = sec.getDouble("basePrice")
                val volatility = sec.getDouble("volatility", 0.10)
                val gradeStr = sec.getString("grade") ?: "COIN"
                val grade = runCatching { AssetGrade.valueOf(gradeStr) }.getOrDefault(AssetGrade.COIN)

                val stock = Stock(id, name, symbol, currentPrice, basePrice, "코인", volatility, 0, grade)
                stockManager.registerStock(stock)
                activeCoins.add(id)
                usedNames.add(name)
            }
        }

        val names = config.getStringList("usedNames")
        usedNames.addAll(names)

        val pendingSection = config.getConfigurationSection("pending")
        if (pendingSection != null) {
            for (key in pendingSection.getKeys(false)) {
                val sec = pendingSection.getConfigurationSection(key) ?: continue
                val gradeStr = sec.getString("grade") ?: "COIN"
                val grade = runCatching { AssetGrade.valueOf(gradeStr) }.getOrDefault(AssetGrade.COIN)
                val ticks = sec.getInt("ticks", REPLACEMENT_DELAY_TICKS)
                pendingReplacements.add(PendingReplacement(grade, ticks))
            }
        }
    }
}
