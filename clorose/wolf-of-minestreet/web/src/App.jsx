import { useState, useMemo } from 'react'
import { useApi } from './hooks/useApi'
import Header from './components/Header'
import NewsTicker from './components/NewsTicker'
import StockTable from './components/StockTable'
import CoinTable from './components/CoinTable'
import DetailPanel from './components/DetailPanel'
import NewsFeed from './components/NewsFeed'
import './App.css'

export default function App() {
  const { stocks, coins, news, connected, lastUpdate, historyRef, fetchOhlc, fetchTrades } = useApi()
  const [activeTab, setActiveTab] = useState('stocks')
  const [selectedAsset, setSelectedAsset] = useState(null)

  function handleSelect(asset) {
    setSelectedAsset(prev => prev?.id === asset.id ? null : asset)
  }

  function handleClose() {
    setSelectedAsset(null)
  }

  const freshAsset = selectedAsset
    ? [...stocks, ...coins].find(a => a.id === selectedAsset.id) || selectedAsset
    : null

  const marketSummary = useMemo(() => {
    const all = [...stocks, ...coins]
    const totalVol = all.reduce((s, a) => s + (a.volume || 0), 0)
    const avgChange = all.length > 0
      ? all.reduce((s, a) => s + a.change, 0) / all.length
      : 0
    return { count: all.length, totalVol, avgChange }
  }, [stocks, coins])

  return (
    <>
      <Header
        connected={connected}
        lastUpdate={lastUpdate}
        stockCount={stocks.length}
        coinCount={coins.length}
        summary={marketSummary}
      />

      <NewsTicker news={news} />

      <div className="hts-main">
        {/* Left panel: list */}
        <div className="hts-left">
          <div className="panel-header">
            <span className="panel-title">종목현재가</span>
            <div className="hts-tabs">
              <button
                className={`hts-tab ${activeTab === 'stocks' ? 'active' : ''}`}
                onClick={() => setActiveTab('stocks')}
              >
                주식
              </button>
              <button
                className={`hts-tab ${activeTab === 'coins' ? 'active' : ''}`}
                onClick={() => setActiveTab('coins')}
              >
                코인
              </button>
            </div>
          </div>

          <div className="hts-table-wrap">
            {activeTab === 'stocks' ? (
              <StockTable
                stocks={stocks}
                historyRef={historyRef}
                selectedId={selectedAsset?.id}
                onSelect={handleSelect}
              />
            ) : (
              <CoinTable
                coins={coins}
                historyRef={historyRef}
                selectedId={selectedAsset?.id}
                onSelect={handleSelect}
              />
            )}
          </div>
        </div>

        {/* Right panel: detail */}
        <div className="hts-right">
          {freshAsset ? (
            <DetailPanel
              asset={freshAsset}
              fetchOhlc={fetchOhlc}
              fetchTrades={fetchTrades}
              onClose={handleClose}
            />
          ) : (
            <div className="hts-empty-detail">
              <div className="hts-empty-icon">&#9776;</div>
              <div className="hts-empty-text">종목을 선택하세요</div>
            </div>
          )}
        </div>
      </div>

      <NewsFeed news={news} />
    </>
  )
}
