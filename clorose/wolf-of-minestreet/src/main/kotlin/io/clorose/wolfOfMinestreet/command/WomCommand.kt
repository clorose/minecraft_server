package io.clorose.wolfOfMinestreet.command

import io.clorose.wolfOfMinestreet.WolfOfMinestreet
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class WomCommand(private val plugin: WolfOfMinestreet) : CommandExecutor, TabCompleter {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("플레이어만 사용할 수 있습니다.")
            return true
        }

        if (args.isEmpty()) {
            sendHelp(sender)
            return true
        }

        when (args[0].lowercase()) {
            "list" -> handleList(sender)
            "buy" -> handleBuy(sender, args)
            "sell" -> handleSell(sender, args)
            "info" -> handleInfo(sender, args)
            "portfolio", "pf" -> handlePortfolio(sender)
            else -> sendHelp(sender)
        }

        return true
    }

    private fun handleList(player: Player) {
        val stocks = plugin.stockManager.getAllStocks()
        if (stocks.isEmpty()) {
            player.sendMessage("§c등록된 종목이 없습니다.")
            return
        }

        player.sendMessage("§e§l=== 종목 목록 ===")
        stocks.forEach { stock ->
            player.sendMessage("§7[§6${stock.symbol}§7] §f${stock.name} §7- §a${String.format("%.2f", stock.currentPrice)}원")
        }
    }

    private fun handleBuy(player: Player, args: Array<out String>) {
        if (args.size < 3) {
            player.sendMessage("§c사용법: /wom buy <종목> <수량>")
            return
        }

        val symbol = args[1]
        val amount = args[2].toLongOrNull()

        if (amount == null || amount <= 0) {
            player.sendMessage("§c올바른 수량을 입력하세요.")
            return
        }

        val stock = plugin.stockManager.getStockBySymbol(symbol)
        if (stock == null) {
            player.sendMessage("§c존재하지 않는 종목입니다.")
            return
        }

        val totalCost = stock.currentPrice * amount
        val economy = plugin.economy

        if (economy == null) {
            player.sendMessage("§c경제 시스템을 사용할 수 없습니다.")
            return
        }

        // 잔액 확인
        if (economy.getBalance(player) < totalCost) {
            player.sendMessage("§c돈이 부족합니다. 필요: ${String.format("%.2f", totalCost)}원, 보유: ${String.format("%.2f", economy.getBalance(player))}원")
            return
        }

        // 돈 차감
        economy.withdrawPlayer(player, totalCost)

        // 주식 추가
        plugin.portfolioManager.buyStock(player.uniqueId, stock.id, amount)

        player.sendMessage("§a매수 완료: ${stock.name} ${amount}주 (${String.format("%.2f", totalCost)}원)")
        player.sendMessage("§7잔액: §f${String.format("%.2f", economy.getBalance(player))}원")
    }

    private fun handleSell(player: Player, args: Array<out String>) {
        if (args.size < 3) {
            player.sendMessage("§c사용법: /wom sell <종목> <수량>")
            return
        }

        val symbol = args[1]
        val amount = args[2].toLongOrNull()

        if (amount == null || amount <= 0) {
            player.sendMessage("§c올바른 수량을 입력하세요.")
            return
        }

        val stock = plugin.stockManager.getStockBySymbol(symbol)
        if (stock == null) {
            player.sendMessage("§c존재하지 않는 종목입니다.")
            return
        }

        val economy = plugin.economy

        if (economy == null) {
            player.sendMessage("§c경제 시스템을 사용할 수 없습니다.")
            return
        }

        // 보유 수량 확인 및 주식 제거
        val success = plugin.portfolioManager.sellStock(player.uniqueId, stock.id, amount)
        if (!success) {
            player.sendMessage("§c보유 수량이 부족합니다.")
            return
        }

        val totalRevenue = stock.currentPrice * amount

        // 돈 지급
        economy.depositPlayer(player, totalRevenue)

        player.sendMessage("§a매도 완료: ${stock.name} ${amount}주 (${String.format("%.2f", totalRevenue)}원)")
        player.sendMessage("§7잔액: §f${String.format("%.2f", economy.getBalance(player))}원")
    }

    private fun handleInfo(player: Player, args: Array<out String>) {
        if (args.size < 2) {
            player.sendMessage("§c사용법: /wom info <종목>")
            return
        }

        val symbol = args[1]
        val stock = plugin.stockManager.getStockBySymbol(symbol)

        if (stock == null) {
            player.sendMessage("§c존재하지 않는 종목입니다.")
            return
        }

        val change = ((stock.currentPrice - stock.basePrice) / stock.basePrice) * 100
        val changeColor = if (change >= 0) "§a" else "§c"

        player.sendMessage("§e§l=== ${stock.name} (${stock.symbol}) ===")
        player.sendMessage("§7현재가: §f${String.format("%.2f", stock.currentPrice)}원")
        player.sendMessage("§7기준가: §f${String.format("%.2f", stock.basePrice)}원")
        player.sendMessage("§7변동률: ${changeColor}${String.format("%+.2f", change)}%")
        player.sendMessage("§7거래량: §f${stock.totalVolume}주")
    }

    private fun handlePortfolio(player: Player) {
        val portfolio = plugin.portfolioManager.getPortfolio(player.uniqueId)

        if (portfolio.holdings.isEmpty()) {
            player.sendMessage("§c보유 종목이 없습니다.")
            return
        }

        player.sendMessage("§e§l=== 내 포트폴리오 ===")
        var totalValue = 0.0

        portfolio.holdings.forEach { (stockId, amount) ->
            val stock = plugin.stockManager.getStock(stockId)
            if (stock != null) {
                val value = stock.currentPrice * amount
                totalValue += value
                player.sendMessage("§7[§6${stock.symbol}§7] §f${amount}주 §7- §a${String.format("%.2f", value)}원")
            }
        }

        player.sendMessage("§e총 평가액: §a${String.format("%.2f", totalValue)}원")
    }

    private fun sendHelp(player: Player) {
        player.sendMessage("§e§l=== Wolf of Minestreet ===")
        player.sendMessage("§7/wom list §f- 종목 목록")
        player.sendMessage("§7/wom buy <종목> <수량> §f- 매수")
        player.sendMessage("§7/wom sell <종목> <수량> §f- 매도")
        player.sendMessage("§7/wom info <종목> §f- 종목 정보")
        player.sendMessage("§7/wom portfolio §f- 내 포트폴리오")
    }

    override fun onTabComplete(sender: CommandSender, command: Command, label: String, args: Array<out String>): List<String>? {
        if (args.size == 1) {
            return listOf("list", "buy", "sell", "info", "portfolio").filter { it.startsWith(args[0].lowercase()) }
        }

        if (args.size == 2 && args[0].lowercase() in listOf("buy", "sell", "info")) {
            return plugin.stockManager.getAllStocks().map { it.symbol }.filter { it.lowercase().startsWith(args[1].lowercase()) }
        }

        return null
    }
}
