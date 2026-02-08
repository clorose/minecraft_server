package io.clorose.wolfOfMinestreet.command

import io.clorose.wolfOfMinestreet.WolfOfMinestreet
import io.clorose.wolfOfMinestreet.model.AssetGrade
import io.clorose.wolfOfMinestreet.model.TradeSide
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class WomCommand(private val plugin: WolfOfMinestreet) : CommandExecutor, TabCompleter {

    companion object {
        const val SELL_FEE_RATE = 0.01  // 매도 수수료 1%
    }

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
            "coins" -> handleCoins(sender)
            "buy" -> handleBuy(sender, args)
            "sell" -> handleSell(sender, args)
            "info" -> handleInfo(sender, args)
            "portfolio", "pf" -> handlePortfolio(sender)
            "news" -> handleNews(sender)
            else -> sendHelp(sender)
        }

        return true
    }

    private fun handleList(player: Player) {
        val stocks = plugin.stockManager.getAllStocks().filter { !it.grade.isCoin }
        if (stocks.isEmpty()) {
            player.sendMessage("§c등록된 종목이 없습니다.")
            return
        }

        player.sendMessage("§e§l=== 주식 목록 ===")
        val grouped = stocks.groupBy { it.category }
        for ((category, catStocks) in grouped) {
            player.sendMessage("§7--- ${category} ---")
            catStocks.forEach { stock ->
                val change = ((stock.currentPrice - stock.basePrice) / stock.basePrice) * 100
                val changeStr = if (change >= 0) "§a+${String.format("%.1f", change)}%" else "§c${String.format("%.1f", change)}%"
                val gradeColor = stock.grade.colorCode
                val gradeLabel = stock.grade.displayName
                player.sendMessage("${gradeColor}[${gradeLabel}] §7[§6${stock.symbol}§7] §f${stock.name} §7- §f${String.format("%.2f", stock.currentPrice)}원 $changeStr")
            }
        }
    }

    private fun handleCoins(player: Player) {
        val coins = plugin.coinManager.getActiveCoins()
        if (coins.isEmpty()) {
            player.sendMessage("§c현재 활성 코인이 없습니다.")
            return
        }

        player.sendMessage("§d§l=== 코인 목록 ===")
        coins.forEach { coin ->
            val change = ((coin.currentPrice - coin.basePrice) / coin.basePrice) * 100
            val changeStr = if (change >= 0) "§a+${String.format("%.1f", change)}%" else "§c${String.format("%.1f", change)}%"
            val gradeColor = coin.grade.colorCode
            val gradeLabel = coin.grade.displayName
            player.sendMessage("${gradeColor}[${gradeLabel}] §7[§6${coin.symbol}§7] §f${coin.name} §7- §f${String.format("%.2f", coin.currentPrice)}원 $changeStr")
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

        // VI 체크 (주식만)
        if (stock.grade.viEnabled && plugin.priceEngine.isStockFrozen(stock.id)) {
            player.sendMessage("§c${stock.name}(${stock.symbol})은(는) VI 발동으로 거래가 정지되었습니다.")
            return
        }

        val totalCost = stock.currentPrice * amount
        val economy = plugin.economy

        if (economy == null) {
            player.sendMessage("§c경제 시스템을 사용할 수 없습니다.")
            return
        }

        if (economy.getBalance(player) < totalCost) {
            player.sendMessage("§c돈이 부족합니다. 필요: ${String.format("%.2f", totalCost)}원, 보유: ${String.format("%.2f", economy.getBalance(player))}원")
            return
        }

        economy.withdrawPlayer(player, totalCost)
        plugin.portfolioManager.buyStock(player.uniqueId, stock.id, amount, stock.currentPrice)
        stock.totalVolume += amount
        plugin.tradeRecorder.recordTrade(player.name, stock.symbol, stock.id, TradeSide.BUY, amount, stock.currentPrice)
        plugin.saveData()

        val label = if (stock.grade.isCoin) "개" else "주"
        player.sendMessage("§a매수 완료: ${stock.name} ${amount}${label} (${String.format("%.2f", totalCost)}원)")
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

        if (stock.grade.viEnabled && plugin.priceEngine.isStockFrozen(stock.id)) {
            player.sendMessage("§c${stock.name}(${stock.symbol})은(는) VI 발동으로 거래가 정지되었습니다.")
            return
        }

        val economy = plugin.economy
        if (economy == null) {
            player.sendMessage("§c경제 시스템을 사용할 수 없습니다.")
            return
        }

        val success = plugin.portfolioManager.sellStock(player.uniqueId, stock.id, amount)
        if (!success) {
            player.sendMessage("§c보유 수량이 부족합니다.")
            return
        }

        val totalRevenue = stock.currentPrice * amount
        val fee = totalRevenue * SELL_FEE_RATE
        val netRevenue = totalRevenue - fee
        economy.depositPlayer(player, netRevenue)
        stock.totalVolume += amount
        plugin.tradeRecorder.recordTrade(player.name, stock.symbol, stock.id, TradeSide.SELL, amount, stock.currentPrice)
        plugin.saveData()

        val label = if (stock.grade.isCoin) "개" else "주"
        player.sendMessage("§a매도 완료: ${stock.name} ${amount}${label} (${String.format("%.2f", netRevenue)}원)")
        player.sendMessage("§7수수료: §c${String.format("%.2f", fee)}원 §7(1%)")
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
        val gradeColor = stock.grade.colorCode

        player.sendMessage("§e§l=== ${stock.name} (${stock.symbol}) ===")
        player.sendMessage("§7등급: ${gradeColor}${stock.grade.displayName}")
        player.sendMessage("§7업종: §f${stock.category}")
        player.sendMessage("§7현재가: §f${String.format("%.2f", stock.currentPrice)}원")
        player.sendMessage("§7기준가: §f${String.format("%.2f", stock.basePrice)}원")
        player.sendMessage("§7변동률: ${changeColor}${String.format("%+.2f", change)}%")
        player.sendMessage("§7거래량: §f${stock.totalVolume}")
        if (!stock.grade.isCoin) {
            player.sendMessage("§7발행주식수: §f${formatNumber(stock.totalShares)}주")
            player.sendMessage("§7시가총액: §f${formatNumber(stock.marketCap.toLong())}원")
        }

        // 보유 중이면 수익률 표시 (주식+코인 공통)
        val holding = plugin.portfolioManager.getPortfolio(player.uniqueId).getAmount(stock.id)
        if (holding > 0) {
            val label = if (stock.grade.isCoin) "개" else "주"
            val avgPrice = plugin.portfolioManager.getAvgBuyPrice(player.uniqueId, stock.id)
            if (avgPrice > 0) {
                val pnl = ((stock.currentPrice - avgPrice) / avgPrice) * 100
                val pnlColor = if (pnl >= 0) "§a" else "§c"
                val evalValue = stock.currentPrice * holding
                val profitLoss = evalValue - (avgPrice * holding)
                val plSign = if (profitLoss >= 0) "§a+" else "§c"
                player.sendMessage("§7내 보유: §f${holding}${label}")
                player.sendMessage("§7평균매수가: §f${String.format("%.2f", avgPrice)}원")
                player.sendMessage("§7수익률: ${pnlColor}${String.format("%+.1f", pnl)}%")
                player.sendMessage("§7평가손익: ${plSign}${String.format("%.2f", profitLoss)}원")
                if (stock.grade.isCoin) {
                    val liquidationPrice = avgPrice * 0.5
                    player.sendMessage("§7청산 기준가: §c${String.format("%.2f", liquidationPrice)}원")
                }
            }
        }
    }

    private fun handlePortfolio(player: Player) {
        val portfolio = plugin.portfolioManager.getPortfolio(player.uniqueId)

        if (portfolio.holdings.isEmpty()) {
            player.sendMessage("§c보유 종목이 없습니다.")
            return
        }

        player.sendMessage("§e§l=== 내 포트폴리오 ===")
        var totalValue = 0.0
        var totalInvested = 0.0

        // 주식
        val stockHoldings = portfolio.holdings.filter { (id, _) ->
            val s = plugin.stockManager.getStock(id)
            s != null && !s.grade.isCoin
        }
        if (stockHoldings.isNotEmpty()) {
            player.sendMessage("§7--- 주식 ---")
            stockHoldings.forEach { (stockId, amount) ->
                val stock = plugin.stockManager.getStock(stockId) ?: return@forEach
                val value = stock.currentPrice * amount
                totalValue += value
                val avgPrice = plugin.portfolioManager.getAvgBuyPrice(player.uniqueId, stockId)
                if (avgPrice > 0) totalInvested += avgPrice * amount
                val pnl = if (avgPrice > 0) ((stock.currentPrice - avgPrice) / avgPrice) * 100 else 0.0
                val pnlStr = if (avgPrice > 0) {
                    val color = if (pnl >= 0) "§a" else "§c"
                    " ${color}(${String.format("%+.1f", pnl)}%)"
                } else ""
                player.sendMessage("§7[§6${stock.symbol}§7] §f${amount}주 §7- §a${String.format("%.2f", value)}원$pnlStr")
            }
        }

        // 코인
        val coinHoldings = portfolio.holdings.filter { (id, _) ->
            val s = plugin.stockManager.getStock(id)
            s != null && s.grade.isCoin
        }
        if (coinHoldings.isNotEmpty()) {
            player.sendMessage("§7--- 코인 ---")
            coinHoldings.forEach { (stockId, amount) ->
                val stock = plugin.stockManager.getStock(stockId) ?: return@forEach
                val value = stock.currentPrice * amount
                totalValue += value
                val avgPrice = plugin.portfolioManager.getAvgBuyPrice(player.uniqueId, stockId)
                if (avgPrice > 0) totalInvested += avgPrice * amount
                val pnl = if (avgPrice > 0) ((stock.currentPrice - avgPrice) / avgPrice) * 100 else 0.0
                val pnlStr = if (avgPrice > 0) {
                    val color = if (pnl >= 0) "§a" else "§c"
                    " ${color}(${String.format("%+.1f", pnl)}%)"
                } else ""
                player.sendMessage("§d[${stock.symbol}] §f${amount}개 §7- §f${String.format("%.2f", value)}원$pnlStr")
            }
        }

        player.sendMessage("§e총 평가액: §a${String.format("%.2f", totalValue)}원")
        if (totalInvested > 0) {
            val totalPnl = ((totalValue - totalInvested) / totalInvested) * 100
            val profitLoss = totalValue - totalInvested
            val plColor = if (profitLoss >= 0) "§a" else "§c"
            player.sendMessage("§e총 손익: ${plColor}${String.format("%+.2f", profitLoss)}원 (${String.format("%+.1f", totalPnl)}%)")
        }
    }

    private fun handleNews(player: Player) {
        val newsList = plugin.newsManager.getRecentNews(10)
        if (newsList.isEmpty()) {
            player.sendMessage("§7최근 뉴스가 없습니다.")
            return
        }

        player.sendMessage("§e§l=== WOM 속보 ===")
        newsList.forEach { news ->
            val timeAgo = (System.currentTimeMillis() - news.timestamp) / 1000
            val timeStr = when {
                timeAgo < 60 -> "${timeAgo}초 전"
                timeAgo < 3600 -> "${timeAgo / 60}분 전"
                else -> "${timeAgo / 3600}시간 전"
            }
            val icon = if (news.bullish) "§a▲" else "§c▼"
            player.sendMessage("§7[$timeStr] $icon §f${news.headline}")
        }
    }

    private fun sendHelp(player: Player) {
        player.sendMessage("§e§l=== Wolf of Minestreet ===")
        player.sendMessage("§7/wom list §f- 주식 목록")
        player.sendMessage("§7/wom coins §f- 코인 목록")
        player.sendMessage("§7/wom buy <종목> <수량> §f- 매수")
        player.sendMessage("§7/wom sell <종목> <수량> §f- 매도")
        player.sendMessage("§7/wom info <종목> §f- 종목 정보")
        player.sendMessage("§7/wom portfolio §f- 내 포트폴리오")
        player.sendMessage("§7/wom news §f- 최근 뉴스")
    }

    private fun formatNumber(value: Long): String {
        return when {
            value >= 100_000_000 -> "${String.format("%.1f", value / 100_000_000.0)}억"
            value >= 10_000 -> "${String.format("%.1f", value / 10_000.0)}만"
            else -> String.format("%,d", value)
        }
    }

    override fun onTabComplete(sender: CommandSender, command: Command, label: String, args: Array<out String>): List<String>? {
        if (args.size == 1) {
            return listOf("list", "coins", "buy", "sell", "info", "portfolio", "news").filter { it.startsWith(args[0].lowercase()) }
        }

        if (args.size == 2 && args[0].lowercase() in listOf("buy", "sell", "info")) {
            return plugin.stockManager.getAllStocks().map { it.symbol }.filter { it.lowercase().startsWith(args[1].lowercase()) }
        }

        return null
    }
}
