package io.clorose.wolfOfMinestreet.manager

import io.clorose.wolfOfMinestreet.model.Portfolio
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.util.UUID

class PortfolioManager {
    private val portfolios = mutableMapOf<UUID, Portfolio>()

    // 플레이어별, 종목별 평균 매수가
    private val avgBuyPrices = mutableMapOf<UUID, MutableMap<String, Double>>()

    // 오프라인 청산 알림 대기열
    private val pendingNotifications = mutableMapOf<UUID, MutableList<String>>()

    fun getPortfolio(playerUUID: UUID): Portfolio {
        return portfolios.getOrPut(playerUUID) { Portfolio(playerUUID) }
    }

    fun buyStock(playerUUID: UUID, stockId: String, amount: Long, pricePerUnit: Double) {
        val portfolio = getPortfolio(playerUUID)
        val currentAmount = portfolio.getAmount(stockId)
        val currentAvg = getAvgBuyPrice(playerUUID, stockId)

        // 가중평균 매수가 갱신
        val newAvg = if (currentAmount == 0L) {
            pricePerUnit
        } else {
            (currentAmount * currentAvg + amount * pricePerUnit) / (currentAmount + amount)
        }

        portfolio.addStock(stockId, amount)
        avgBuyPrices.getOrPut(playerUUID) { mutableMapOf() }[stockId] = newAvg
    }

    fun sellStock(playerUUID: UUID, stockId: String, amount: Long): Boolean {
        val portfolio = getPortfolio(playerUUID)
        val success = portfolio.removeStock(stockId, amount)
        if (!success) return false

        // 전량 매도 시 평균가 삭제, 부분 매도 시 평균가 불변
        if (portfolio.getAmount(stockId) == 0L) {
            avgBuyPrices[playerUUID]?.remove(stockId)
        }
        return true
    }

    fun getAvgBuyPrice(playerUUID: UUID, stockId: String): Double {
        return avgBuyPrices[playerUUID]?.get(stockId) ?: 0.0
    }

    /** 강제 청산: 전량 매도 + 평균가 삭제 */
    fun liquidate(playerUUID: UUID, stockId: String): Long {
        val portfolio = getPortfolio(playerUUID)
        val amount = portfolio.getAmount(stockId)
        if (amount <= 0) return 0

        portfolio.holdings.remove(stockId)
        avgBuyPrices[playerUUID]?.remove(stockId)
        return amount
    }

    /** 상장폐지: 전량 0원 처리 + 평균가 삭제 */
    fun delistForPlayer(playerUUID: UUID, stockId: String): Long {
        return liquidate(playerUUID, stockId)
    }

    /** 특정 종목 보유자 전체 조회 */
    fun getHolders(stockId: String): Map<UUID, Long> {
        val result = mutableMapOf<UUID, Long>()
        for ((uuid, portfolio) in portfolios) {
            val amount = portfolio.getAmount(stockId)
            if (amount > 0) result[uuid] = amount
        }
        return result
    }

    // === 오프라인 알림 ===

    fun addPendingNotification(playerUUID: UUID, message: String) {
        pendingNotifications.getOrPut(playerUUID) { mutableListOf() }.add(message)
    }

    fun popPendingNotifications(playerUUID: UUID): List<String> {
        return pendingNotifications.remove(playerUUID) ?: emptyList()
    }

    // === 저장/로드 ===

    fun save(dataFolder: File) {
        // 포트폴리오 저장
        val config = YamlConfiguration()
        portfolios.forEach { (uuid, portfolio) ->
            portfolio.holdings.forEach { (stockId, amount) ->
                config.set("portfolios.$uuid.$stockId", amount)
            }
        }
        // 평균 매수가 저장
        avgBuyPrices.forEach { (uuid, prices) ->
            prices.forEach { (stockId, avg) ->
                config.set("avgBuyPrice.$uuid.$stockId", avg)
            }
        }
        // 알림 저장
        pendingNotifications.forEach { (uuid, messages) ->
            config.set("notifications.$uuid", messages)
        }
        config.save(File(dataFolder, "portfolios.yml"))
    }

    fun load(dataFolder: File) {
        val file = File(dataFolder, "portfolios.yml")
        if (!file.exists()) return

        val config = YamlConfiguration.loadConfiguration(file)

        // 포트폴리오 로드
        val section = config.getConfigurationSection("portfolios")
        if (section != null) {
            for (uuidStr in section.getKeys(false)) {
                val uuid = try { UUID.fromString(uuidStr) } catch (_: IllegalArgumentException) { continue }
                val holdingsSection = section.getConfigurationSection(uuidStr) ?: continue
                val holdings = mutableMapOf<String, Long>()
                for (stockId in holdingsSection.getKeys(false)) {
                    holdings[stockId] = holdingsSection.getLong(stockId)
                }
                portfolios[uuid] = Portfolio(uuid, holdings)
            }
        }

        // 평균 매수가 로드
        val avgSection = config.getConfigurationSection("avgBuyPrice")
        if (avgSection != null) {
            for (uuidStr in avgSection.getKeys(false)) {
                val uuid = try { UUID.fromString(uuidStr) } catch (_: IllegalArgumentException) { continue }
                val pricesSection = avgSection.getConfigurationSection(uuidStr) ?: continue
                val prices = mutableMapOf<String, Double>()
                for (stockId in pricesSection.getKeys(false)) {
                    prices[stockId] = pricesSection.getDouble(stockId)
                }
                avgBuyPrices[uuid] = prices
            }
        }

        // 알림 로드
        val notifSection = config.getConfigurationSection("notifications")
        if (notifSection != null) {
            for (uuidStr in notifSection.getKeys(false)) {
                val uuid = try { UUID.fromString(uuidStr) } catch (_: IllegalArgumentException) { continue }
                val messages = config.getStringList("notifications.$uuidStr")
                if (messages.isNotEmpty()) {
                    pendingNotifications[uuid] = messages.toMutableList()
                }
            }
        }
    }
}
