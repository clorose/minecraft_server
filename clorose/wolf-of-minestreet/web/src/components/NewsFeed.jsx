function timeAgo(ts) {
  const sec = Math.floor((Date.now() - ts) / 1000)
  if (sec < 60) return `${sec}초`
  if (sec < 3600) return `${Math.floor(sec / 60)}분`
  return `${Math.floor(sec / 3600)}시간`
}

const tierLabel = { NORMAL: '속보', MAJOR: '긴급', EXTREME: '초대형' }

export default function NewsFeed({ news }) {
  return (
    <div className="hts-bottom-news">
      <div className="news-panel-header">NEWS FEED</div>
      <div className="news-list">
        {news.length === 0 ? (
          <div className="news-empty">뉴스 대기 중...</div>
        ) : (
          news.map((n, i) => (
            <div className="news-row" key={`${n.timestamp}-${i}`}>
              <span className="news-time">{timeAgo(n.timestamp)}</span>
              <span className={`news-tier tier-${n.tier}`}>
                {tierLabel[n.tier] || n.tier}
              </span>
              <span className={`news-arrow ${n.bullish ? 'bull' : 'bear'}`}>
                {n.bullish ? '▲' : '▼'}
              </span>
              <span className="news-headline">{n.headline}</span>
            </div>
          ))
        )}
      </div>
    </div>
  )
}
