import Sparkline from './Sparkline'

export default function StockRow({ stock, history, selected, onClick }) {
  const cls = stock.change > 0.01 ? 'price-up' : stock.change < -0.01 ? 'price-down' : 'price-flat'
  const arrow = stock.change > 0.01 ? '▲' : stock.change < -0.01 ? '▼' : ''
  const sign = stock.change > 0.01 ? '+' : ''

  return (
    <tr className={selected ? 'row-selected' : ''} onClick={() => onClick(stock)}>
      <td className="cell-name">
        <div className="stock-symbol">{stock.symbol}</div>
        <div className="stock-name">{stock.name}</div>
      </td>
      <td className="cell-grade">
        <span className={`grade-tag grade-${stock.grade}`}>{stock.gradeDisplay}</span>
      </td>
      <td className={`cell-price ${cls}`}>
        {stock.currentPrice.toFixed(2)}
        {stock.frozen && <span className="vi-badge">VI</span>}
      </td>
      <td className={`cell-change ${cls}`}>
        {arrow}{sign}{stock.change.toFixed(2)}%
      </td>
      <td className="cell-vol">{(stock.volume || 0).toLocaleString()}</td>
      <td className="cell-spark">
        <Sparkline history={history} />
      </td>
    </tr>
  )
}
