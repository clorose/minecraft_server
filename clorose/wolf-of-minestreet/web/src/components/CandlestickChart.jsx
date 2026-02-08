import { useEffect, useRef } from 'react'
import { createChart, ColorType, CandlestickSeries } from 'lightweight-charts'

export default function CandlestickChart({ candles }) {
  const containerRef = useRef(null)
  const chartRef = useRef(null)
  const seriesRef = useRef(null)

  useEffect(() => {
    if (!containerRef.current) return

    const chart = createChart(containerRef.current, {
      width: containerRef.current.clientWidth,
      height: 300,
      layout: {
        background: { type: ColorType.Solid, color: '#fafafa' },
        textColor: '#666',
        fontSize: 11,
      },
      grid: {
        vertLines: { color: '#eee' },
        horzLines: { color: '#eee' },
      },
      crosshair: {
        mode: 0,
      },
      rightPriceScale: {
        borderColor: '#ddd',
      },
      timeScale: {
        borderColor: '#ddd',
        timeVisible: true,
        secondsVisible: false,
      },
    })

    const series = chart.addSeries(CandlestickSeries, {
      upColor: '#d12b1e',
      downColor: '#1a5dab',
      borderUpColor: '#d12b1e',
      borderDownColor: '#1a5dab',
      wickUpColor: '#d12b1e',
      wickDownColor: '#1a5dab',
    })

    chartRef.current = chart
    seriesRef.current = series

    const handleResize = () => {
      if (containerRef.current) {
        chart.applyOptions({ width: containerRef.current.clientWidth })
      }
    }
    window.addEventListener('resize', handleResize)

    return () => {
      window.removeEventListener('resize', handleResize)
      chart.remove()
      chartRef.current = null
      seriesRef.current = null
    }
  }, [])

  useEffect(() => {
    if (seriesRef.current && candles && candles.length > 0) {
      seriesRef.current.setData(candles)
      chartRef.current?.timeScale().fitContent()
    }
  }, [candles])

  return <div ref={containerRef} className="chart-container" />
}
