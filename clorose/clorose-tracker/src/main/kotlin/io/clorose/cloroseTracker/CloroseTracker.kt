package io.clorose.cloroseTracker

import io.clorose.cloroseTracker.data.CsvWriter
import io.clorose.cloroseTracker.listener.PlayerSessionListener
import io.clorose.cloroseTracker.listener.ShopGuiListener
import net.milkbowl.vault.economy.Economy
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask

class CloroseTracker : JavaPlugin() {

    private lateinit var csvWriter: CsvWriter
    private lateinit var economy: Economy
    private var hourlyTask: BukkitTask? = null

    override fun onEnable() {
        val eco = setupEconomy()
        if (eco == null) {
            logger.severe("Vault Economy를 찾을 수 없습니다! 플러그인을 비활성화합니다.")
            server.pluginManager.disablePlugin(this)
            return
        }
        economy = eco

        csvWriter = CsvWriter(dataFolder)
        csvWriter.init()

        // ShopGUI+ (soft depend)
        if (server.pluginManager.getPlugin("ShopGUIPlus") != null) {
            val shopListener = ShopGuiListener(this, csvWriter)
            server.pluginManager.registerEvents(shopListener, this)
            logger.info("ShopGUI+ 연동 활성화")
        } else {
            logger.info("ShopGUI+ 플러그인 없음 - 상점 거래 추적 비활성화")
        }

        // PlayerSession (항상 활성)
        val sessionListener = PlayerSessionListener(this, csvWriter, economy)
        server.pluginManager.registerEvents(sessionListener, this)

        // 1시간 타이머 (20 ticks/sec * 60 * 60 = 72000 ticks)
        hourlyTask = server.scheduler.runTaskTimerAsynchronously(this, Runnable {
            hourlySnapshot()
        }, 72000L, 72000L)

        logger.info("clorose-tracker 활성화 (CSV 모드)")
    }

    override fun onDisable() {
        hourlyTask?.cancel()

        if (::economy.isInitialized) {
            for (player in server.onlinePlayers) {
                val balance = economy.getBalance(player)
                csvWriter.writeBalance(player.uniqueId, player.name, balance, "SHUTDOWN")
            }
        }

        logger.info("clorose-tracker 비활성화")
    }

    private fun hourlySnapshot() {
        if (::economy.isInitialized) {
            for (player in server.onlinePlayers) {
                val balance = economy.getBalance(player)
                csvWriter.writeBalance(player.uniqueId, player.name, balance, "HOURLY")
            }
        }
    }

    private fun setupEconomy(): Economy? {
        val rsp = server.servicesManager.getRegistration(Economy::class.java) ?: return null
        return rsp.provider
    }
}
