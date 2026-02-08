package io.clorose.cloroseTracker.listener

import io.clorose.cloroseTracker.data.CsvWriter
import io.clorose.cloroseTracker.model.TransactionType
import net.brcdev.shopgui.event.ShopPostTransactionEvent
import net.brcdev.shopgui.shop.ShopManager.ShopAction
import net.brcdev.shopgui.shop.ShopTransactionResult.ShopTransactionResultType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

class ShopGuiListener(
    private val plugin: JavaPlugin,
    private val csvWriter: CsvWriter,
) : Listener {

    @EventHandler
    fun onShopTransaction(event: ShopPostTransactionEvent) {
        val result = event.result
        if (result.result != ShopTransactionResultType.SUCCESS) return

        val player = result.player
        val uuid = player.uniqueId
        val name = player.name
        val price = result.price
        val quantity = result.amount
        val itemName = result.shopItem.item?.type?.name ?: "UNKNOWN"
        val detail = "$itemName x$quantity"

        val (type, amount) = when (result.shopAction) {
            ShopAction.BUY -> TransactionType.SHOP_BUY to -price
            ShopAction.SELL, ShopAction.SELL_ALL -> TransactionType.SHOP_SELL to price
        }

        plugin.server.scheduler.runTaskAsynchronously(plugin, Runnable {
            csvWriter.writeTransaction(uuid, name, type, amount, detail)
        })
    }
}
