export default function Header({ connected, lastUpdate, stockCount, coinCount, summary }) {
  const changeClass = summary.avgChange > 0.01 ? 'price-up' : summary.avgChange < -0.01 ? 'price-down' : 'price-flat'
  const arrow = summary.avgChange > 0.01 ? '▲' : summary.avgChange < -0.01 ? '▼' : '─'

  return (
    <div className="hts-titlebar">
      <div className="hts-titlebar-left">
        <span className="hts-logo">WOM HTS</span>
        <span className="hts-version">Wolf of Minestreet</span>
      </div>
      <div className="hts-titlebar-right">
        <div className="hts-market-summary">
          <div className="hts-summary-item">
            <span className="label">주식</span>
            <span className="value">{stockCount}</span>
          </div>
          <div className="hts-summary-item">
            <span className="label">코인</span>
            <span className="value">{coinCount}</span>
          </div>
          <div className="hts-summary-item">
            <span className="label">시장</span>
            <span className={`value ${changeClass}`}>
              {arrow}{Math.abs(summary.avgChange).toFixed(1)}%
            </span>
          </div>
          <div className="hts-summary-item">
            <span className="label">거래</span>
            <span className="value">{summary.totalVol.toLocaleString()}</span>
          </div>
        </div>
        <div className="hts-status">
          <span className={`hts-status-dot ${connected ? 'live' : 'off'}`} />
          <span style={{ color: connected ? '#00e676' : '#ff3b30', fontSize: 10, fontWeight: 700 }}>
            {connected ? 'LIVE' : 'OFF'}
          </span>
        </div>
        {lastUpdate && (
          <span className="hts-time">{lastUpdate.toLocaleTimeString('ko-KR')}</span>
        )}
      </div>
    </div>
  )
}
