import { useState } from 'react';
import { tradeAPI } from '../services/api';

export default function TradePanel({
  symbol, currentPrice, selectedChallenge, onTradePlaced
}) {
  const [lotSize, setLotSize] = useState('0.01');
  const [stopLoss, setStopLoss] = useState('');
  const [takeProfit, setTakeProfit] = useState('');
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState(null);

  const handleTrade = async (direction) => {
    if (!selectedChallenge) {
      setMessage({
        type: 'error',
        text: 'Please select a challenge first!'
      });
      return;
    }

    setLoading(true);
    setMessage(null);

    try {
      const response = await tradeAPI.openTrade({
        challengeAttemptId: selectedChallenge.id,
        symbol,
        direction,
        lotSize: parseFloat(lotSize),
        stopLoss: stopLoss ? parseFloat(stopLoss) : null,
        takeProfit: takeProfit ? parseFloat(takeProfit) : null,
      });

      const trade = response.data.data;
      setMessage({
        type: 'success',
        text: `${direction} ${symbol} opened at ${trade.openPrice}`
      });
      onTradePlaced();
    } catch (err) {
      setMessage({
        type: 'error',
        text: err.response?.data?.message || 'Trade failed'
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-4">
      <h3 className="text-white font-semibold text-lg">
        Place Trade
      </h3>

      {/* Current Price */}
      <div className="bg-slate-700 rounded-lg p-3 text-center">
        <div className="text-slate-400 text-xs">Current Price</div>
        <div className="text-white font-bold text-xl">
          {currentPrice > 100
            ? currentPrice.toFixed(2)
            : currentPrice.toFixed(5)}
        </div>
        <div className="text-blue-400 text-sm font-semibold">
          {symbol}
        </div>
      </div>

      {/* Lot Size */}
      <div>
        <label className="text-slate-400 text-sm mb-1 block">
          Lot Size
        </label>
        <input
          type="number"
          value={lotSize}
          onChange={(e) => setLotSize(e.target.value)}
          step="0.01"
          min="0.01"
          max="100"
          className="w-full bg-slate-700 text-white rounded-lg
              px-3 py-2 border border-slate-600
              focus:border-blue-500 focus:outline-none"
        />
      </div>

      {/* Stop Loss */}
      <div>
        <label className="text-slate-400 text-sm mb-1 block">
          Stop Loss (optional)
        </label>
        <input
          type="number"
          value={stopLoss}
          onChange={(e) => setStopLoss(e.target.value)}
          step="0.00001"
          className="w-full bg-slate-700 text-white rounded-lg
              px-3 py-2 border border-slate-600
              focus:border-blue-500 focus:outline-none"
          placeholder="0.00000"
        />
      </div>

      {/* Take Profit */}
      <div>
        <label className="text-slate-400 text-sm mb-1 block">
          Take Profit (optional)
        </label>
        <input
          type="number"
          value={takeProfit}
          onChange={(e) => setTakeProfit(e.target.value)}
          step="0.00001"
          className="w-full bg-slate-700 text-white rounded-lg
              px-3 py-2 border border-slate-600
              focus:border-blue-500 focus:outline-none"
          placeholder="0.00000"
        />
      </div>

      {/* Buy/Sell Buttons */}
      <div className="grid grid-cols-2 gap-3">
        <button
          onClick={() => handleTrade('BUY')}
          disabled={loading}
          className="bg-green-600 hover:bg-green-700 text-white
              font-bold py-4 rounded-lg transition-colors
              disabled:opacity-50 text-lg"
        >
          BUY
        </button>
        <button
          onClick={() => handleTrade('SELL')}
          disabled={loading}
          className="bg-red-600 hover:bg-red-700 text-white
              font-bold py-4 rounded-lg transition-colors
              disabled:opacity-50 text-lg"
        >
          SELL
        </button>
      </div>

      {/* Message */}
      {message && (
        <div className={`p-3 rounded-lg text-sm
            ${message.type === 'success'
              ? 'bg-green-500/20 text-green-400 border border-green-500'
              : 'bg-red-500/20 text-red-400 border border-red-500'}`}>
          {message.text}
        </div>
      )}
    </div>
  );
}