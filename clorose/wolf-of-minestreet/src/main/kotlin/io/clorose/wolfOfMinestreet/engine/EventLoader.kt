package io.clorose.wolfOfMinestreet.engine

import io.clorose.wolfOfMinestreet.manager.StockManager
import io.clorose.wolfOfMinestreet.model.*
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.util.logging.Logger

/**
 * companies/ 폴더 구조를 스캔하여 종목 + 이벤트를 로드.
 *
 * companies/
 *   _market.yml              ← 시장 전체 이벤트
 *   food/
 *     _common.yml            ← 식품 업종 공통 이벤트
 *     pandafandb.yml         ← 회사 정의 + 전용 이벤트
 *   mining/
 *     _common.yml
 *     diamondcorp.yml
 */
class EventLoader(
    private val logger: Logger
) {

    fun load(companiesDir: File, stockManager: StockManager): EventRegistry {
        val registry = EventRegistry()

        if (!companiesDir.exists() || !companiesDir.isDirectory) {
            logger.warning("companies/ 폴더가 없습니다: ${companiesDir.path}")
            return registry
        }

        // 1) _market.yml — 시장 전체 이벤트
        val marketFile = File(companiesDir, "_market.yml")
        if (marketFile.exists()) {
            val config = YamlConfiguration.loadConfiguration(marketFile)
            val events = parseEventList(config, "events")
            registry.marketEvents.addAll(events)
            logger.info("시장 이벤트 ${events.size}개 로드 (_market.yml)")
        }

        // 2) 하위 폴더 = 업종
        val categoryDirs = companiesDir.listFiles { f -> f.isDirectory } ?: return registry

        for (catDir in categoryDirs) {
            val category = catDir.name

            // 업종 공통 이벤트
            val commonFile = File(catDir, "_common.yml")
            if (commonFile.exists()) {
                val config = YamlConfiguration.loadConfiguration(commonFile)
                val events = parseEventList(config, "events")
                registry.categoryEvents.getOrPut(category) { mutableListOf() }.addAll(events)
                logger.info("${category} 업종 공통 이벤트 ${events.size}개 로드")
            }

            // 개별 회사 파일
            val companyFiles = catDir.listFiles { f ->
                f.isFile && f.extension == "yml" && f.name != "_common.yml"
            } ?: continue

            for (companyFile in companyFiles) {
                val config = YamlConfiguration.loadConfiguration(companyFile)
                val stockId = companyFile.nameWithoutExtension

                // 종목 등록
                val name = config.getString("name") ?: stockId
                val symbol = config.getString("symbol") ?: stockId.uppercase()
                val basePrice = config.getDouble("basePrice", 100.0)
                val volatility = config.getDouble("volatility", 0.05)
                val gradeStr = config.getString("grade")
                val grade = gradeStr?.let {
                    runCatching { AssetGrade.valueOf(it.uppercase()) }.getOrNull()
                } ?: AssetGrade.GROWTH
                val totalShares = config.getLong("totalShares", 10_000)

                stockManager.registerStock(
                    Stock(stockId, name, symbol, basePrice, basePrice, category, volatility, grade = grade, totalShares = totalShares)
                )

                // 회사 전용 이벤트
                val events = parseEventList(config, "events")
                if (events.isNotEmpty()) {
                    registry.companyEvents.getOrPut(stockId) { mutableListOf() }.addAll(events)
                }

                logger.info("${category}/${stockId}: ${name}(${symbol}) 로드 (이벤트 ${events.size}개)")
            }
        }

        return registry
    }

    private fun parseEventList(config: YamlConfiguration, key: String): List<StoryEvent> {
        val list = config.getMapList(key)
        return list.mapNotNull { parseEvent(it) }
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseEvent(map: Any?): StoryEvent? {
        val m = map as? Map<String, Any?> ?: return null
        val headline = m["headline"] as? String ?: return null

        val tierStr = m["tier"] as? String
        val tier = tierStr?.let { runCatching { EventTier.valueOf(it.uppercase()) }.getOrNull() } ?: EventTier.NORMAL

        val bullish = m["bullish"] as? Boolean
        val jumpMin = (m["jumpMin"] as? Number)?.toDouble() ?: tier.jumpMin
        val jumpMax = (m["jumpMax"] as? Number)?.toDouble() ?: tier.jumpMax
        val probability = (m["probability"] as? Number)?.toDouble() ?: 0.03
        val successRate = (m["successRate"] as? Number)?.toDouble()

        val success = (m["success"] as? Map<String, Any?>)?.let { parseOutcome(it) }
        val failure = (m["failure"] as? Map<String, Any?>)?.let { parseOutcome(it) }

        return StoryEvent(headline, tier, bullish, jumpMin, jumpMax, probability, successRate, success, failure)
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseOutcome(m: Map<String, Any?>): StoryOutcome {
        val headline = m["headline"] as? String ?: ""
        val jump = (m["jump"] as? Number)?.toDouble() ?: 0.0
        val tierStr = m["tier"] as? String
        val tier = tierStr?.let { runCatching { EventTier.valueOf(it.uppercase()) }.getOrNull() } ?: EventTier.NORMAL
        val followUp = (m["followUp"] as? Map<String, Any?>)?.let { parseFollowUp(it) }
        return StoryOutcome(headline, jump, tier, followUp)
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseFollowUp(m: Map<String, Any?>): StoryFollowUp {
        val delayTicks = (m["delayTicks"] as? Number)?.toInt() ?: 3
        val headline = m["headline"] as? String ?: ""
        val jump = (m["jump"] as? Number)?.toDouble() ?: 0.0
        val tierStr = m["tier"] as? String
        val tier = tierStr?.let { runCatching { EventTier.valueOf(it.uppercase()) }.getOrNull() } ?: EventTier.NORMAL
        return StoryFollowUp(delayTicks, headline, jump, tier)
    }
}
