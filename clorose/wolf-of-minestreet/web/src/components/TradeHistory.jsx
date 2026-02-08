function timeAgo(ts) {
  const sec = Math.floor((Date.now() - ts) / 1000)
  if (sec < 60) return `${sec}초`
  if (sec < 3600) return `${Math.floor(sec / 60)}분`
  return `${Math.floor(sec / 3600)}시간`
}

export default function TradeHistory({ trades }) {
  if (!trades || trades.length === 0) {
    return <div className="trade-empty">체결 내역 없음</div>
  }

  return (
    <>
      <div className="trades-header">체결 내역</div>
      <table className="trade-table">
        <thead>
          <tr>
            <th>시간</th>
            <th>플레이어</th>
            <th>유형</th>
            <th style={{ textAlign: 'right' }}>수량</th>
            <th style={{ textAlign: 'right' }}>가격</th>
          </tr>
        </thead>
        <tbody>
          {trades.map((t, i) => (
            <tr key={`${t.timestamp}-${i}`}>
              <td className="trade-time">{timeAgo(t.timestamp)}</td>
              <td>{t.playerName}</td>
              <td className={t.side === 'BUY' ? 'trade-buy' : 'trade-sell'}>
                {t.side === 'BUY' ? '매수' : '매도'}
              </td>
              <td style={{ textAlign: 'right' }}>{t.quantity.toLocaleString()}</td>
              <td style={{ textAlign: 'right' }}>{t.price.toFixed(2)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </>
  )
}
