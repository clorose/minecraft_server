import { useState, useEffect, useRef, useCallback } from 'react'

const API = 'http://localhost:8080/api'
const POLL_MS = 3000

async function safeFetch(url) {
  try {
    const res = await fetch(url)
    if (!res.ok) return null
    return await res.json()
  } catch {
    return null
  }
}

export function useApi() {
  const [stocks, setStocks] = useState([])
  const [coins, setCoins] = useState([])
  const [news, setNews] = useState([])
  const [connected, setConnected] = useState(false)
  const [lastUpdate, setLastUpdate] = useState(null)
  const historyRef = useRef({})

  useEffect(() => {
    let active = true

    async function poll() {
      const [stockData, coinData, newsData] = await Promise.all([
        safeFetch(`${API}/stocks`),
        safeFetch(`${API}/coins`),
        safeFetch(`${API}/news`),
      ])

      if (!active) return

      const gotStocks = stockData?.stocks ?? []
      const gotCoins = coinData?.coins ?? []
      const gotNews = newsData?.news ?? []

      const hist = { ...historyRef.current }
      for (const s of gotStocks) {
        if (!hist[s.id]) hist[s.id] = []
        hist[s.id].push(s.currentPrice)
        if (hist[s.id].length > 30) hist[s.id] = hist[s.id].slice(-30)
      }
      for (const c of gotCoins) {
        if (!hist[c.id]) hist[c.id] = []
        hist[c.id].push(c.currentPrice)
        if (hist[c.id].length > 30) hist[c.id] = hist[c.id].slice(-30)
      }
      historyRef.current = hist

      setStocks(gotStocks)
      setCoins(gotCoins)
      setNews(gotNews)
      setConnected(stockData !== null || coinData !== null)
      setLastUpdate(new Date())
    }

    poll()
    const id = setInterval(poll, POLL_MS)
    return () => { active = false; clearInterval(id) }
  }, [])

  const fetchOhlc = useCallback(async (symbol) => {
    try {
      const res = await fetch(`${API}/ohlc?symbol=${encodeURIComponent(symbol)}`)
      const data = await res.json()
      return data.candles || []
    } catch {
      return []
    }
  }, [])

  const fetchTrades = useCallback(async (symbol) => {
    try {
      const url = symbol
        ? `${API}/trades?symbol=${encodeURIComponent(symbol)}`
        : `${API}/trades`
      const res = await fetch(url)
      const data = await res.json()
      return data.trades || []
    } catch {
      return []
    }
  }, [])

  return { stocks, coins, news, connected, lastUpdate, historyRef, fetchOhlc, fetchTrades }
}
