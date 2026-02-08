package io.clorose.wolfOfMinestreet.web

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import io.clorose.wolfOfMinestreet.engine.CoinManager
import io.clorose.wolfOfMinestreet.engine.NewsManager
import io.clorose.wolfOfMinestreet.engine.PriceEngine
import io.clorose.wolfOfMinestreet.engine.TradeRecorder
import io.clorose.wolfOfMinestreet.manager.StockManager
import java.net.InetSocketAddress
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Logger

class WebServer(
    private val stockManager: StockManager,
    private val newsManager: NewsManager,
    private val priceEngine: PriceEngine,
    private val tradeRecorder: TradeRecorder,
    private val coinManager: CoinManager,
    private val logger: Logger,
    private val port: Int = 8080
) {

    private var server: HttpServer? = null
    private val refPrices = ConcurrentHashMap<String, Double>()
    private var resetTimer: Timer? = null

    fun start() {
        // 기준가 스냅샷
        snapshotRefPrices()

        // 1시간마다 기준가 리셋
        resetTimer = Timer("wom-ref-reset", true).also { timer ->
            timer.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    snapshotRefPrices()
                    logger.info("WOM 웹 기준가 리셋 완료")
                }
            }, 3600_000L, 3600_000L)
        }

        try {
            server = HttpServer.create(InetSocketAddress(port), 0).apply {
                createContext("/api/stocks") { handle(it, ::handleStocks) }
                createContext("/api/coins") { handle(it, ::handleCoins) }
                createContext("/api/news") { handle(it, ::handleNews) }
                createContext("/api/ohlc") { handleWithExchange(it, ::handleOhlc) }
                createContext("/api/trades") { handleWithExchange(it, ::handleTrades) }
                executor = null
                start()
            }
            logger.info("WOM WebServer 시작 (포트 $port)")
        } catch (e: Exception) {
            logger.warning("WOM WebServer 시작 실패: ${e.message}")
        }
    }

    fun stop() {
        resetTimer?.cancel()
        resetTimer = null
        server?.stop(0)
        server = null
        logger.info("WOM WebServer 중지")
    }

    private fun snapshotRefPrices() {
        stockManager.getAllStocks().forEach { stock ->
            refPrices[stock.id] = stock.currentPrice
        }
        coinManager.getActiveCoins().forEach { coin ->
            refPrices[coin.id] = coin.currentPrice
        }
    }

    private fun handle(exchange: HttpExchange, handler: () -> String) {
        try {
            exchange.responseHeaders.add("Content-Type", "application/json; charset=utf-8")
            exchange.responseHeaders.add("Access-Control-Allow-Origin", "*")
            exchange.responseHeaders.add("Access-Control-Allow-Methods", "GET")

            if (exchange.requestMethod == "OPTIONS") {
                exchange.sendResponseHeaders(204, -1)
                return
            }

            val body = handler().toByteArray(Charsets.UTF_8)
            exchange.sendResponseHeaders(200, body.size.toLong())
            exchange.responseBody.use { it.write(body) }
        } catch (e: Exception) {
            val err = """{"error":"${esc(e.message ?: "unknown")}"}""".toByteArray(Charsets.UTF_8)
            exchange.sendResponseHeaders(500, err.size.toLong())
            exchange.responseBody.use { it.write(err) }
        }
    }

    private fun handleWithExchange(exchange: HttpExchange, handler: (HttpExchange) -> String) {
        try {
            exchange.responseHeaders.add("Content-Type", "application/json; charset=utf-8")
            exchange.responseHeaders.add("Access-Control-Allow-Origin", "*")
            exchange.responseHeaders.add("Access-Control-Allow-Methods", "GET")

            if (exchange.requestMethod == "OPTIONS") {
                exchange.sendResponseHeaders(204, -1)
                return
            }

            val body = handler(exchange).toByteArray(Charsets.UTF_8)
            exchange.sendResponseHeaders(200, body.size.toLong())
            exchange.responseBody.use { it.write(body) }
        } catch (e: Exception) {
            val err = """{"error":"${esc(e.message ?: "unknown")}"}""".toByteArray(Charsets.UTF_8)
            exchange.sendResponseHeaders(500, err.size.toLong())
            exchange.responseBody.use { it.write(err) }
        }
    }

    private fun parseQuery(exchange: HttpExchange): Map<String, String> {
        val query = exchange.requestURI.rawQuery ?: return emptyMap()
        return query.split("&").mapNotNull {
            val parts = it.split("=", limit = 2)
            if (parts.size == 2) parts[0] to java.net.URLDecoder.decode(parts[1], "UTF-8") else null
        }.toMap()
    }

    private fun handleStocks(): String {
        val stocks = stockManager.getAllStocks().filter { !it.grade.isCoin }
        val sb = StringBuilder()
        sb.append("""{"stocks":[""")
        stocks.forEachIndexed { i, stock ->
            if (i > 0) sb.append(",")
            val ref = refPrices[stock.id] ?: stock.currentPrice
            val change = if (ref > 0) ((stock.currentPrice - ref) / ref) * 100 else 0.0
            val frozen = priceEngine.isStockFrozen(stock.id)
            sb.append("""{""")
            sb.append(""""id":"${esc(stock.id)}",""")
            sb.append(""""name":"${esc(stock.name)}",""")
            sb.append(""""symbol":"${esc(stock.symbol)}",""")
            sb.append(""""category":"${esc(stock.category)}",""")
            sb.append(""""currentPrice":${stock.currentPrice},""")
            sb.append(""""basePrice":${ref},""")
            sb.append(""""change":${String.format("%.4f", change)},""")
            sb.append(""""volume":${stock.totalVolume},""")
            sb.append(""""frozen":$frozen,""")
            sb.append(""""grade":"${stock.grade.name}",""")
            sb.append(""""gradeDisplay":"${esc(stock.grade.displayName)}"""")
            sb.append("""}""")
        }
        sb.append("""],"timestamp":${System.currentTimeMillis()}}""")
        return sb.toString()
    }

    private fun handleCoins(): String {
        val coins = coinManager.getActiveCoins()
        val sb = StringBuilder()
        sb.append("""{"coins":[""")
        coins.forEachIndexed { i, coin ->
            if (i > 0) sb.append(",")
            val ref = refPrices[coin.id] ?: coin.currentPrice
            val change = if (ref > 0) ((coin.currentPrice - ref) / ref) * 100 else 0.0
            sb.append("""{""")
            sb.append(""""id":"${esc(coin.id)}",""")
            sb.append(""""name":"${esc(coin.name)}",""")
            sb.append(""""symbol":"${esc(coin.symbol)}",""")
            sb.append(""""category":"${esc(coin.category)}",""")
            sb.append(""""currentPrice":${coin.currentPrice},""")
            sb.append(""""basePrice":${ref},""")
            sb.append(""""change":${String.format("%.4f", change)},""")
            sb.append(""""volume":${coin.totalVolume},""")
            sb.append(""""grade":"${coin.grade.name}",""")
            sb.append(""""gradeDisplay":"${esc(coin.grade.displayName)}"""")
            sb.append("""}""")
        }
        sb.append("""],"timestamp":${System.currentTimeMillis()}}""")
        return sb.toString()
    }

    private fun handleNews(): String {
        val newsList = newsManager.getRecentNews(20)
        val sb = StringBuilder()
        sb.append("""{"news":[""")
        newsList.forEachIndexed { i, news ->
            if (i > 0) sb.append(",")
            sb.append("""{""")
            sb.append(""""timestamp":${news.timestamp},""")
            sb.append(""""headline":"${esc(stripColor(news.headline))}",""")
            sb.append(""""category":"${esc(news.category)}",""")
            sb.append(""""bullish":${news.bullish},""")
            sb.append(""""impact":${String.format("%.2f", news.impactPercent)},""")
            sb.append(""""tier":"${news.tier.name}"""")
            sb.append("""}""")
        }
        sb.append("""],"timestamp":${System.currentTimeMillis()}}""")
        return sb.toString()
    }

    private fun handleOhlc(exchange: HttpExchange): String {
        val params = parseQuery(exchange)
        val symbol = params["symbol"]
        if (symbol.isNullOrBlank()) {
            return """{"error":"symbol parameter required","candles":[]}"""
        }

        val stock = stockManager.getStockBySymbol(symbol)
            ?: return """{"error":"unknown symbol","candles":[]}"""

        val candles = tradeRecorder.getCandles(stock.id)
        val sb = StringBuilder()
        sb.append("""{"symbol":"${esc(symbol)}","candles":[""")
        candles.forEachIndexed { i, c ->
            if (i > 0) sb.append(",")
            sb.append("""{""")
            sb.append(""""time":${c.time},""")
            sb.append(""""open":${c.open},""")
            sb.append(""""high":${c.high},""")
            sb.append(""""low":${c.low},""")
            sb.append(""""close":${c.close},""")
            sb.append(""""volume":${c.volume}""")
            sb.append("""}""")
        }
        sb.append("""]}""")
        return sb.toString()
    }

    private fun handleTrades(exchange: HttpExchange): String {
        val params = parseQuery(exchange)
        val symbol = params["symbol"]

        val stockId = if (!symbol.isNullOrBlank()) {
            stockManager.getStockBySymbol(symbol)?.id
        } else null

        val trades = tradeRecorder.getRecentTrades(50, stockId)
        val sb = StringBuilder()
        sb.append("""{"trades":[""")
        trades.forEachIndexed { i, t ->
            if (i > 0) sb.append(",")
            sb.append("""{""")
            sb.append(""""timestamp":${t.timestamp},""")
            sb.append(""""playerName":"${esc(t.playerName)}",""")
            sb.append(""""symbol":"${esc(t.symbol)}",""")
            sb.append(""""side":"${t.side.name}",""")
            sb.append(""""quantity":${t.quantity},""")
            sb.append(""""price":${t.price}""")
            sb.append("""}""")
        }
        sb.append("""]}""")
        return sb.toString()
    }

    private fun esc(s: String): String = s
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")

    private fun stripColor(s: String): String = s.replace(Regex("§[0-9a-fk-or]", RegexOption.IGNORE_CASE), "")
}
