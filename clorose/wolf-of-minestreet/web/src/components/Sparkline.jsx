export default function Sparkline({ history }) {
  if (!history || history.length < 2) return null
  const min = Math.min(...history)
  const max = Math.max(...history)
  const range = max - min || 1
  const last = history[history.length - 1]
  const prev = history[history.length - 2]
  // Korean HTS: red=up, blue=down
  const color = last >= prev ? 'var(--up)' : 'var(--down)'

  return (
    <div className="sparkline">
      {history.map((v, i) => (
        <div
          key={i}
          className="spark-bar"
          style={{
            height: `${Math.max(((v - min) / range) * 100, 4)}%`,
            background: i === history.length - 1 ? color : 'var(--dim)',
            opacity: i === history.length - 1 ? 1 : 0.35,
          }}
        />
      ))}
    </div>
  )
}
