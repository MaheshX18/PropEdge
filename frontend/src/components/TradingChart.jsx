import { useEffect, useRef } from 'react';
import { createChart } from 'lightweight-charts';

export default function TradingChart({ symbol, currentPrice }) {
  const chartContainerRef = useRef(null);
  const chartRef = useRef(null);
  const seriesRef = useRef(null);
  const priceHistoryRef = useRef([]);

  useEffect(() => {
    if (!chartContainerRef.current) return;

    // Create chart
    const chart = createChart(chartContainerRef.current, {
      layout: {
        background: { color: '#0f172a' },
        textColor: '#94a3b8',
      },
      grid: {
        vertLines: { color: '#1e293b' },
        horzLines: { color: '#1e293b' },
      },
      crosshair: {
        mode: 1,
      },
      rightPriceScale: {
        borderColor: '#334155',
      },
      timeScale: {
        borderColor: '#334155',
        timeVisible: true,
        secondsVisible: false,
      },
      width: chartContainerRef.current.clientWidth,
      height: 400,
    });

    // Create candlestick series
    const candleSeries = chart.addCandlestickSeries({
      upColor: '#22c55e',
      downColor: '#ef4444',
      borderUpColor: '#22c55e',
      borderDownColor: '#ef4444',
      wickUpColor: '#22c55e',
      wickDownColor: '#ef4444',
    });

    chartRef.current = chart;
    seriesRef.current = candleSeries;

    // Handle resize
    const handleResize = () => {
      chart.applyOptions({
        width: chartContainerRef.current.clientWidth,
      });
    };
    window.addEventListener('resize', handleResize);

    return () => {
      window.removeEventListener('resize', handleResize);
      chart.remove();
    };
  }, []);

  // Update chart with new price
  useEffect(() => {
    if (!seriesRef.current || !currentPrice) return;

    const now = Math.floor(Date.now() / 1000);
    const minute = Math.floor(now / 60) * 60;
    const history = priceHistoryRef.current;

    if (history.length === 0 || history[history.length - 1].time !== minute) {
      // New candle
      const newCandle = {
        time: minute,
        open: currentPrice,
        high: currentPrice,
        low: currentPrice,
        close: currentPrice,
      };
      history.push(newCandle);
      if (history.length > 200) history.shift();
    } else {
      // Update current candle
      const current = history[history.length - 1];
      current.close = currentPrice;
      current.high = Math.max(current.high, currentPrice);
      current.low = Math.min(current.low, currentPrice);
    }

    seriesRef.current.setData([...history]);
  }, [currentPrice]);

  return (
    <div className="flex-1 bg-slate-900 relative">
      <div className="absolute top-3 left-3 z-10">
        <span className="text-white font-bold text-lg">
          {symbol}
        </span>
        <span className="text-slate-400 text-sm ml-3">
          {currentPrice > 100
            ? currentPrice.toFixed(2)
            : currentPrice.toFixed(5)}
        </span>
      </div>
      <div ref={chartContainerRef} className="w-full" />
    </div>
  );
}