import Sparkline from './Sparkline'

export default function CoinRow({ coin, history, selected, onClick }) {
  const cls = coin.change > 0.01 ? 'price-up' : coin.change < -0.01 ? 'price-down' : 'price-flat'
  const arrow = coin.change > 0.01 ? '▲' : coin.change < -0.01 ? '▼' : ''
  const sign = coin.change > 0.01 ? '+' : ''

  return (
    <tr className={selected ? 'row-selected' : ''} onClick={() => onClick(coin)}>
      <td className="cell-name">
        <div className="stock-symbol">{coin.symbol}</div>
        <div className="stock-name">{coin.name}</div>
      </td>
      <td className="cell-grade">
        <span className={`grade-tag grade-${coin.grade}`}>{coin.gradeDisplay}</span>
      </td>
      <td className={`cell-price ${cls}`}>
        {coin.currentPrice.toFixed(2)}
      </td>
      <td className={`cell-change ${cls}`}>
        {arrow}{sign}{coin.change.toFixed(2)}%
      </td>
      <td className="cell-vol">{(coin.volume || 0).toLocaleString()}</td>
      <td className="cell-spark">
        <Sparkline history={history} />
      </td>
    </tr>
  )
}
