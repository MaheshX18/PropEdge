import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { useQuery } from '@tanstack/react-query';
import { marketAPI, challengeAPI, tradeAPI } from '../services/api';
import TradingChart from '../components/TradingChart';
import TradePanel from '../components/TradePanel';
import ChallengeStatus from '../components/ChallengeStatus';
import PriceBar from '../components/PriceBar';
import AICoach from '../components/AICoach';

export default function Dashboard() {
  const { user, logout } = useAuth();
  const [selectedSymbol, setSelectedSymbol] = useState('BTCUSDT');
  const [selectedChallenge, setSelectedChallenge] = useState(null);
  const [showAI, setShowAI] = useState(false);

  const { data: prices } = useQuery({
    queryKey: ['prices'],
    queryFn: () => marketAPI.getAllPrices().then(r => r.data),
    refetchInterval: 5000,
  });

  const { data: challenges, refetch: refetchChallenges } = useQuery({
    queryKey: ['challenges'],
    queryFn: () => challengeAPI.getMyChallenges().then(r => r.data),
  });

  const { data: openTrades, refetch: refetchTrades } = useQuery({
    queryKey: ['openTrades'],
    queryFn: () => tradeAPI.getOpenTrades().then(r => r.data),
    refetchInterval: 5000,
  });

  useEffect(() => {
    if (challenges && challenges.length > 0 && !selectedChallenge) {
      const active = challenges.find(c => c.status === 'ACTIVE');
      if (active) setSelectedChallenge(active);
    }
  }, [challenges]);

  const currentPrice = prices?.[selectedSymbol] || 0;

  return (
    <div className="min-h-screen bg-slate-900 flex flex-col">

      {/* Top Navigation */}
      <nav className="bg-slate-800 border-b border-slate-700
          px-6 py-3 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <span className="text-xl font-bold text-white">
            📈 PropPractice
          </span>
        </div>
        <div className="flex items-center gap-4">
          <button
            onClick={() => setShowAI(!showAI)}
            className={`text-sm px-3 py-1 rounded-lg
                transition-colors
                ${showAI
                  ? 'bg-purple-600 text-white'
                  : 'text-slate-400 hover:text-white'}`}
          >
            🤖 AI Coach
          </button>
          <span className="text-slate-400 text-sm">
            {user?.fullName}
          </span>
          <button
            onClick={logout}
            className="text-slate-400 hover:text-white
                text-sm transition-colors"
          >
            Logout
          </button>
        </div>
      </nav>

      {/* Price Bar */}
      <PriceBar
        prices={prices}
        selectedSymbol={selectedSymbol}
        onSelectSymbol={setSelectedSymbol}
      />

      {/* Main Content */}
      <div className="flex flex-1 overflow-hidden">

        {/* Left Sidebar */}
        <div className="w-72 bg-slate-800 border-r
            border-slate-700 p-4 overflow-y-auto">
          <ChallengeStatus
            challenges={challenges}
            selectedChallenge={selectedChallenge}
            onSelectChallenge={setSelectedChallenge}
            onRefresh={refetchChallenges}
          />
        </div>

        {/* Main Chart Area */}
        <div className="flex-1 flex flex-col overflow-hidden">
          <TradingChart
            symbol={selectedSymbol}
            currentPrice={currentPrice}
          />

          {/* Open Trades Table */}
          {openTrades && openTrades.length > 0 && (
            <div className="bg-slate-800 border-t
                border-slate-700 p-4 max-h-48 overflow-y-auto">
              <h3 className="text-white font-semibold mb-3">
                Open Trades
              </h3>
              <table className="w-full text-sm">
                <thead>
                  <tr className="text-slate-400">
                    <th className="text-left pb-2">Symbol</th>
                    <th className="text-left pb-2">Dir</th>
                    <th className="text-left pb-2">Lots</th>
                    <th className="text-left pb-2">Open</th>
                    <th className="text-left pb-2">Current</th>
                    <th className="text-left pb-2">P&L</th>
                    <th className="text-left pb-2">Action</th>
                  </tr>
                </thead>
                <tbody>
                  {openTrades.map(trade => {
                    const current = prices?.[trade.symbol] || 0;
                    const multiplier =
                        trade.symbol.endsWith('USDT') ? 1 :
                        trade.symbol === 'XAUUSD' ? 100 : 100000;
                    const pnl = trade.direction === 'BUY'
                      ? (current - trade.openPrice)
                          * trade.lotSize * multiplier
                      : (trade.openPrice - current)
                          * trade.lotSize * multiplier;

                    return (
                      <tr key={trade.tradeId}
                          className="border-t border-slate-700">
                        <td className="py-2 text-white">
                          {trade.symbol}
                        </td>
                        <td className={`py-2 font-semibold
                            ${trade.direction === 'BUY'
                              ? 'text-green-400'
                              : 'text-red-400'}`}>
                          {trade.direction}
                        </td>
                        <td className="py-2 text-slate-300">
                          {trade.lotSize}
                        </td>
                        <td className="py-2 text-slate-300">
                          {trade.openPrice > 100
                            ? trade.openPrice.toFixed(2)
                            : trade.openPrice.toFixed(5)}
                        </td>
                        <td className="py-2 text-slate-300">
                          {current > 100
                            ? current.toFixed(2)
                            : current.toFixed(5)}
                        </td>
                        <td className={`py-2 font-semibold
                            ${pnl >= 0
                              ? 'text-green-400'
                              : 'text-red-400'}`}>
                          {pnl >= 0 ? '+' : ''}${pnl.toFixed(2)}
                        </td>
                        <td className="py-2">
                          <button
                            onClick={async () => {
                              await tradeAPI.closeTrade(
                                  trade.tradeId);
                              refetchTrades();
                              refetchChallenges();
                            }}
                            className="bg-red-600 hover:bg-red-700
                                text-white text-xs px-3 py-1
                                rounded transition-colors"
                          >
                            Close
                          </button>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          )}
        </div>

        {/* Right Sidebar */}
        <div className="w-72 bg-slate-800 border-l
            border-slate-700 p-4 overflow-y-auto flex flex-col gap-4">
          <TradePanel
            symbol={selectedSymbol}
            currentPrice={currentPrice}
            selectedChallenge={selectedChallenge}
            onTradePlaced={() => {
              refetchTrades();
              refetchChallenges();
            }}
          />

          {/* AI Coach Panel */}
          {showAI && (
            <div className="border-t border-slate-700 pt-4">
              <AICoach
                challenges={challenges}
                openTrades={openTrades}
              />
            </div>
          )}
        </div>
      </div>
    </div>
  );
}