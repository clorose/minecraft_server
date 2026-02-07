package io.clorose.wolfOfMinestreet

import io.clorose.wolfOfMinestreet.command.WomCommand
import io.clorose.wolfOfMinestreet.manager.PortfolioManager
import io.clorose.wolfOfMinestreet.manager.StockManager
import io.clorose.wolfOfMinestreet.model.Stock
import net.milkbowl.vault.economy.Economy
import org.bukkit.plugin.java.JavaPlugin

class WolfOfMinestreet : JavaPlugin() {

    lateinit var stockManager: StockManager
    lateinit var portfolioManager: PortfolioManager
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

        // 명령어 등록
        val womCommand = WomCommand(this)
        getCommand("wom")?.setExecutor(womCommand)
        getCommand("wom")?.tabCompleter = womCommand

        // 테스트용 종목 등록
        registerDefaultStocks()

        logger.info("Wolf of Minestreet 활성화 완료")
    }

    override fun onDisable() {
        logger.info("Wolf of Minestreet 비활성화 완료")
    }

    private fun registerDefaultStocks() {
        stockManager.registerStock(Stock("diamond", "다이아몬드", "DIA", 100.0, 100.0))
        stockManager.registerStock(Stock("emerald", "에메랄드", "EME", 50.0, 50.0))
        stockManager.registerStock(Stock("gold", "금", "GOLD", 30.0, 30.0))
        stockManager.registerStock(Stock("iron", "철", "IRON", 10.0, 10.0))
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
