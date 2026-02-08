import CoinRow from './CoinRow'

export default function CoinTable({ coins, historyRef, selectedId, onSelect }) {
  return (
    <table className="hts-table">
      <thead>
        <tr>
          <th style={{ textAlign: 'left' }}>코인</th>
          <th style={{ textAlign: 'center' }}>등급</th>
          <th>현재가</th>
          <th>등락률</th>
          <th>거래량</th>
          <th>추이</th>
        </tr>
      </thead>
      <tbody>
        {coins.length === 0 ? (
          <tr><td colSpan={6} className="empty-row">활성 코인 없음</td></tr>
        ) : (
          coins.map(c => (
            <CoinRow
              key={c.id}
              coin={c}
              history={historyRef.current[c.id]}
              selected={c.id === selectedId}
              onClick={onSelect}
            />
          ))
        )}
      </tbody>
    </table>
  )
}
