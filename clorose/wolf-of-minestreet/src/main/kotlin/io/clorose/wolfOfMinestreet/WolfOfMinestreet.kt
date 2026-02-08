package io.clorose.wolfOfMinestreet

import io.clorose.wolfOfMinestreet.command.WomCommand
import io.clorose.wolfOfMinestreet.engine.CoinManager
import io.clorose.wolfOfMinestreet.engine.EventLoader
import io.clorose.wolfOfMinestreet.engine.NewsManager
import io.clorose.wolfOfMinestreet.engine.PriceEngine
import io.clorose.wolfOfMinestreet.engine.TradeRecorder
import io.clorose.wolfOfMinestreet.manager.PortfolioManager
import io.clorose.wolfOfMinestreet.manager.StockManager
import io.clorose.wolfOfMinestreet.model.Stock
import io.clorose.wolfOfMinestreet.web.WebServer
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class WolfOfMinestreet : JavaPlugin(), Listener {

    lateinit var stockManager: StockManager
    lateinit var portfolioManager: PortfolioManager
    lateinit var newsManager: NewsManager
    lateinit var priceEngine: PriceEngine
    lateinit var coinManager: CoinManager
    lateinit var tradeRecorder: TradeRecorder
    private var webServer: WebServer? = null
    var economy: Economy? = null

    override fun onEnable() {
        // Vault Economy 서비스 로드
        if (!setupEconomy()) {
            logger.severe("Vault를 찾을 수 없습니다! 플러그인을 비활성화합니다.")
            server.pluginManager.disablePlugin(this)
            return
        }

        stockManager = StockManager()
        portfolioManager = PortfolioManager()
        dataFolder.mkdirs()

        // companies/ 폴더에서 종목 + 이벤트 로드
        val companiesDir = File(dataFolder, "companies")
        val eventLoader = EventLoader(logger)
        val eventRegistry = if (companiesDir.exists() && companiesDir.isDirectory) {
            eventLoader.load(companiesDir, stockManager)
        } else {
            // 폴백: 기존 companies.yml
            saveResourceIfMissing("companies.yml")
            loadCompaniesLegacy()
            null
        }

        // 런타임 데이터 로드 (현재가, 거래량 등)
        stockManager.load(dataFolder)
        portfolioManager.load(dataFolder)

        val stockCount = stockManager.getAllStocks().size
        if (stockCount == 0) {
            logger.warning("등록된 종목이 없습니다! companies/ 폴더를 확인하세요.")
        }

        // 트레이드 레코더
        tradeRecorder = TradeRecorder()

        // 가격 엔진 시작
        newsManager = NewsManager()
        priceEngine = PriceEngine(this, stockManager, ::saveData, tradeRecorder)

        // 코인 매니저
        coinManager = CoinManager(this, stockManager, portfolioManager, newsManager, economy, tradeRecorder)
        coinManager.load(dataFolder)
        coinManager.initialize()
        priceEngine.coinManager = coinManager

        priceEngine.start(newsManager, eventRegistry)

        // 명령어 등록
        val womCommand = WomCommand(this)
        getCommand("wom")?.setExecutor(womCommand)
        getCommand("wom")?.tabCompleter = womCommand

        // 이벤트 리스너 등록 (오프라인 청산 알림)
        server.pluginManager.registerEvents(this, this)

        // 웹 API 서버 시작
        webServer = WebServer(stockManager, newsManager, priceEngine, tradeRecorder, coinManager, logger).also { it.start() }

        val coinCount = coinManager.getActiveCoins().size
        logger.info("Wolf of Minestreet 활성화 완료 (주식 ${stockCount}개, 코인 ${coinCount}개, 가격 엔진 + 웹 API 가동)")
    }

    override fun onDisable() {
        webServer?.stop()
        if (::priceEngine.isInitialized) {
            priceEngine.stop()
        }
        if (::stockManager.isInitialized && ::portfolioManager.isInitialized) {
            saveData()
        }
        logger.info("Wolf of Minestreet 비활성화 완료")
    }

    fun saveData() {
        stockManager.save(dataFolder)
        portfolioManager.save(dataFolder)
        if (::coinManager.isInitialized) {
            coinManager.save(dataFolder)
        }
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val notifications = portfolioManager.popPendingNotifications(event.player.uniqueId)
        if (notifications.isNotEmpty()) {
            Bukkit.getScheduler().runTaskLater(this, Runnable {
                event.player.sendMessage("§e§l=== 부재 중 알림 ===")
                notifications.forEach { event.player.sendMessage(it) }
            }, 40L) // 2초 후 발송
        }
    }

    private fun saveResourceIfMissing(name: String) {
        if (!File(dataFolder, name).exists()) {
            saveResource(name, false)
        }
    }

    /** 기존 companies.yml 호환 로드 */
    private fun loadCompaniesLegacy() {
        val file = File(dataFolder, "companies.yml")
        if (!file.exists()) return
        val config = YamlConfiguration.loadConfiguration(file)
        val section = config.getConfigurationSection("stocks") ?: return
        for (id in section.getKeys(false)) {
            val s = section.getConfigurationSection(id) ?: continue
            val name = s.getString("name") ?: id
            val symbol = s.getString("symbol") ?: id.uppercase()
            val basePrice = s.getDouble("basePrice", 100.0)
            val category = s.getString("category") ?: "기타"
            val volatility = s.getDouble("volatility", 0.05)
            stockManager.registerStock(Stock(id, name, symbol, basePrice, basePrice, category, volatility))
        }
        logger.info("companies.yml에서 종목 ${section.getKeys(false).size}개 로드 (레거시 모드)")
    }

    private fun setupEconomy(): Boolean {
        if (server.pluginManager.getPlugin("Vault") == null) {
            return false
        }
        val rsp = server.servicesManager.getRegistration(Economy::class.java) ?: return false
        economy = rsp.provider
        return true
    }
}
