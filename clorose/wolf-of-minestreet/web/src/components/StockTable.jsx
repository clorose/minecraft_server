import StockRow from './StockRow'

export default function StockTable({ stocks, historyRef, selectedId, onSelect }) {
  return (
    <table className="hts-table">
      <thead>
        <tr>
          <th style={{ textAlign: 'left' }}>종목</th>
          <th style={{ textAlign: 'center' }}>등급</th>
          <th>현재가</th>
          <th>등락률</th>
          <th>거래량</th>
          <th>추이</th>
        </tr>
      </thead>
      <tbody>
        {stocks.length === 0 ? (
          <tr><td colSpan={6} className="empty-row">등록된 종목이 없습니다</td></tr>
        ) : (
          stocks.map(s => (
            <StockRow
              key={s.id}
              stock={s}
              history={historyRef.current[s.id]}
              selected={s.id === selectedId}
              onClick={onSelect}
            />
          ))
        )}
      </tbody>
    </table>
  )
}
