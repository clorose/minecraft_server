package io.clorose.cloroseTracker.listener

import io.clorose.cloroseTracker.data.CsvWriter
import net.milkbowl.vault.economy.Economy
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin

class PlayerSessionListener(
    private val plugin: JavaPlugin,
    private val csvWriter: CsvWriter,
    private val economy: Economy,
) : Listener {

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val player = event.player
        val balance = economy.getBalance(player)
        plugin.server.scheduler.runTaskAsynchronously(plugin, Runnable {
            csvWriter.writeBalance(player.uniqueId, player.name, balance, "JOIN")
        })
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        val player = event.player
        plugin.server.scheduler.runTaskAsynchronously(plugin, Runnable {
            val balance = economy.getBalance(player)
            csvWriter.writeBalance(player.uniqueId, player.name, balance, "QUIT")
        })
    }
}
