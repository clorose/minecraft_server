const tierLabel = { NORMAL: '속보', MAJOR: '긴급', EXTREME: '초대형' }

export default function NewsTicker({ news }) {
  if (!news || news.length === 0) return null

  // Duplicate for seamless loop
  const items = [...news, ...news]

  return (
    <div className="news-ticker">
      <span className="news-ticker-label">NEWS</span>
      <div className="news-ticker-track">
        {items.map((n, i) => (
          <span key={i} className="ticker-item">
            <span className={`ticker-tier ticker-tier-${n.tier}`}>
              {tierLabel[n.tier] || n.tier}
            </span>
            <span className={n.bullish ? 'ticker-arrow-up' : 'ticker-arrow-down'}>
              {n.bullish ? '▲' : '▼'}
            </span>
            <span>{n.headline}</span>
            <span className="ticker-divider">│</span>
          </span>
        ))}
      </div>
    </div>
  )
}
