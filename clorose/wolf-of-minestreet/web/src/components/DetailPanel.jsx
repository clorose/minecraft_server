import { useState, useEffect, useRef } from 'react'
import CandlestickChart from './CandlestickChart'
import TradeHistory from './TradeHistory'

const DETAIL_POLL_MS = 5000

export default function DetailPanel({ asset, fetchOhlc, fetchTrades, onClose }) {
  const [candles, setCandles] = useState([])
  const [trades, setTrades] = useState([])
  const intervalRef = useRef(null)

  useEffect(() => {
    if (!asset) return

    async function loadData() {
      const [c, t] = await Promise.all([
        fetchOhlc(asset.symbol),
        fetchTrades(asset.symbol),
      ])
      setCandles(c)
      setTrades(t)
    }

    loadData()
    intervalRef.current = setInterval(loadData, DETAIL_POLL_MS)

    return () => {
      if (intervalRef.current) clearInterval(intervalRef.current)
    }
  }, [asset, fetchOhlc, fetchTrades])

  if (!asset) return null

  const cls = asset.change > 0.01 ? 'price-up' : asset.change < -0.01 ? 'price-down' : 'price-flat'
  const arrow = asset.change > 0.01 ? '▲' : asset.change < -0.01 ? '▼' : '─'
  const sign = asset.change > 0.01 ? '+' : ''
  const diff = asset.currentPrice - asset.basePrice

  return (
    <>
      <div className="detail-header-bar">
        <div className="detail-header-left">
          <span className="detail-sym">{asset.symbol}</span>
          <span className="detail-nm">{asset.name}</span>
          <span className={`grade-tag grade-${asset.grade}`}>{asset.gradeDisplay}</span>
        </div>
        <button className="detail-close-btn" onClick={onClose}>✕</button>
      </div>

      <div className="detail-price-bar">
        <span className={`detail-current-price ${cls}`}>
          {asset.currentPrice.toFixed(2)}
        </span>
        <span className={`detail-change-info ${cls}`}>
          {arrow} {sign}{diff.toFixed(2)} ({sign}{asset.change.toFixed(2)}%)
        </span>
        <div className="detail-meta">
          <div className="detail-meta-item">
            <span className="meta-label">기준가</span>
            <span className="meta-value">{asset.basePrice.toFixed(2)}</span>
          </div>
          <div className="detail-meta-item">
            <span className="meta-label">거래량</span>
            <span className="meta-value">{(asset.volume || 0).toLocaleString()}</span>
          </div>
        </div>
      </div>

      <div className="detail-chart-area">
        {candles.length > 0 ? (
          <CandlestickChart candles={candles} />
        ) : (
          <div className="chart-placeholder">데이터 수집 중...</div>
        )}
      </div>

      <div className="detail-trades-area">
        <TradeHistory trades={trades} />
      </div>
    </>
  )
}
