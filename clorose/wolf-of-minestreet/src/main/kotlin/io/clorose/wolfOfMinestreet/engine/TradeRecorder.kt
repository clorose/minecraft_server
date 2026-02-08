package io.clorose.wolfOfMinestreet.engine

import io.clorose.wolfOfMinestreet.model.OhlcCandle
import io.clorose.wolfOfMinestreet.model.TradeRecord
import io.clorose.wolfOfMinestreet.model.TradeSide
import java.util.ArrayDeque

class TradeRecorder {

    companion object {
        const val MAX_CANDLES = 60      // 1 hour of 1-min candles
        const val MAX_TRADES = 100
        const val CANDLE_INTERVAL_MS = 60_000L  // 1 minute
    }

    // Per-stock candle data
    private val candles = mutableMapOf<String, ArrayDeque<OhlcCandle>>()
    private val builders = mutableMapOf<String, CandleBuilder>()

    // Recent trades (all stocks)
    private val trades = ArrayDeque<TradeRecord>(MAX_TRADES + 1)

    private class CandleBuilder(
        val minuteStart: Long,  // epoch millis of this minute's start
        var open: Double,
        var high: Double,
        var low: Double,
        var close: Double,
        var volume: Long = 0
    ) {
        fun update(price: Double) {
            close = price
            if (price > high) high = price
            if (price < low) low = price
        }

        fun toCandle(): OhlcCandle = OhlcCandle(
            time = minuteStart / 1000,  // epoch seconds
            open = open, high = high, low = low, close = close,
            volume = volume
        )
    }

    /** Called from PriceEngine.tick() and CoinManager for every price update */
    @Synchronized
    fun updatePrice(stockId: String, price: Double) {
        val now = System.currentTimeMillis()
        val currentMinute = (now / CANDLE_INTERVAL_MS) * CANDLE_INTERVAL_MS

        val builder = builders[stockId]

        if (builder == null || builder.minuteStart != currentMinute) {
            // Finalize previous candle if it exists
            if (builder != null) {
                val deque = candles.getOrPut(stockId) { ArrayDeque(MAX_CANDLES + 1) }
                deque.addLast(builder.toCandle())
                if (deque.size > MAX_CANDLES) deque.removeFirst()
            }
            // Start new candle
            builders[stockId] = CandleBuilder(currentMinute, price, price, price, price)
        } else {
            builder.update(price)
        }
    }

    /** Called from WomCommand on buy/sell */
    @Synchronized
    fun recordTrade(playerName: String, symbol: String, stockId: String, side: TradeSide, quantity: Long, price: Double) {
        val record = TradeRecord(
            timestamp = System.currentTimeMillis(),
            playerName = playerName,
            symbol = symbol,
            stockId = stockId,
            side = side,
            quantity = quantity,
            price = price
        )
        trades.addLast(record)
        if (trades.size > MAX_TRADES) trades.removeFirst()

        // Add volume to current candle builder
        builders[stockId]?.let { it.volume += quantity }
    }

    /** Get finalized candles for a stock (excludes current in-progress candle) */
    @Synchronized
    fun getCandles(stockId: String): List<OhlcCandle> {
        val finalized = candles[stockId]?.toList() ?: emptyList()
        // Also include current in-progress candle as the latest data point
        val current = builders[stockId]?.toCandle()
        return if (current != null) finalized + current else finalized
    }

    /** Get recent trades, optionally filtered by stockId */
    @Synchronized
    fun getRecentTrades(count: Int, stockId: String? = null): List<TradeRecord> {
        val filtered = if (stockId != null) {
            trades.filter { it.stockId == stockId }
        } else {
            trades.toList()
        }
        return filtered.takeLast(count).reversed()  // newest first
    }
}
