import { useState } from 'react';
import { challengeAPI } from '../services/api';

const FIRMS = [
  'FTMO', 'FUNDINGPIPS', 'FUNDED_ROOM',
  'FUNDED_FIRM', 'FUNDEDNEXT', 'FUNDED_FRIDAY',
  'BLUEBERRY_FUNDED', 'GOAT_FUNDED', 'THE5ERS',
  'ALPHA_CAPITAL', 'E8_MARKETS', 'MAVEN'
];

const ACCOUNT_SIZES = [5000, 6000, 10000, 15000,
    25000, 50000, 100000, 200000];

export default function ChallengeStatus({
  challenges, selectedChallenge,
  onSelectChallenge, onRefresh
}) {
  const [showNew, setShowNew] = useState(false);
  const [firm, setFirm] = useState('FTMO');
  const [accountSize, setAccountSize] = useState(10000);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleStartChallenge = async () => {
    setLoading(true);
    setError('');
    try {
      await challengeAPI.startChallenge({ accountSize });
      setShowNew(false);
      onRefresh();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed');
    } finally {
      setLoading(false);
    }
  };

  const getStatusColor = (status) => {
    if (status === 'ACTIVE') return 'text-green-400';
    if (status === 'PASSED') return 'text-blue-400';
    if (status === 'FAILED') return 'text-red-400';
    return 'text-slate-400';
  };

  const getProfitPercent = (challenge) => {
    if (!challenge?.tradingAccount) return 0;
    const { currentBalance, startingBalance } =
        challenge.tradingAccount;
    return ((currentBalance - startingBalance)
        / startingBalance * 100).toFixed(2);
  };

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h3 className="text-white font-semibold">
          My Challenges
        </h3>
        <button
          onClick={() => setShowNew(!showNew)}
          className="bg-blue-600 hover:bg-blue-700 text-white
              text-xs px-3 py-1 rounded-lg transition-colors"
        >
          + New
        </button>
      </div>

      {/* New Challenge Form */}
      {showNew && (
        <div className="bg-slate-700 rounded-lg p-4 space-y-3">
          <div>
            <label className="text-slate-400 text-xs mb-1 block">
              Prop Firm
            </label>
            <select
              value={firm}
              onChange={(e) => setFirm(e.target.value)}
              className="w-full bg-slate-600 text-white
                  rounded px-2 py-2 text-sm border
                  border-slate-500 focus:outline-none"
            >
              {FIRMS.map(f => (
                <option key={f} value={f}>
                  {f.replace(/_/g, ' ')}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="text-slate-400 text-xs mb-1 block">
              Account Size
            </label>
            <select
              value={accountSize}
              onChange={(e) =>
                  setAccountSize(Number(e.target.value))}
              className="w-full bg-slate-600 text-white
                  rounded px-2 py-2 text-sm border
                  border-slate-500 focus:outline-none"
            >
              {ACCOUNT_SIZES.map(size => (
                <option key={size} value={size}>
                  ${size.toLocaleString()}
                </option>
              ))}
            </select>
          </div>

          {error && (
            <p className="text-red-400 text-xs">{error}</p>
          )}

          <button
            onClick={handleStartChallenge}
            disabled={loading}
            className="w-full bg-green-600 hover:bg-green-700
                text-white text-sm py-2 rounded-lg
                transition-colors disabled:opacity-50"
          >
            {loading ? 'Starting...' : 'Start Challenge'}
          </button>
        </div>
      )}

      {/* Challenge List */}
      <div className="space-y-2">
        {challenges?.map(challenge => {
          const profitPercent = getProfitPercent(challenge);
          const isSelected = selectedChallenge?.id === challenge.id;
          const balance =
              challenge.tradingAccount?.currentBalance || 0;
          const startBalance =
              challenge.tradingAccount?.startingBalance || 0;

          return (
            <div
              key={challenge.id}
              onClick={() => onSelectChallenge(challenge)}
              className={`p-3 rounded-lg cursor-pointer
                  border transition-colors
                  ${isSelected
                    ? 'border-blue-500 bg-blue-500/10'
                    : 'border-slate-700 hover:border-slate-500'}`}
            >
              <div className="flex justify-between items-start">
                <div>
                  <div className="text-white text-sm font-semibold">
                    ${startBalance.toLocaleString()}
                  </div>
                  <div className="text-slate-400 text-xs">
                    Challenge #{challenge.id}
                  </div>
                </div>
                <span className={`text-xs font-semibold
                    ${getStatusColor(challenge.status)}`}>
                  {challenge.status}
                </span>
              </div>

              <div className="mt-2">
                <div className="flex justify-between text-xs mb-1">
                  <span className="text-slate-400">Balance</span>
                  <span className="text-white">
                    ${balance.toLocaleString()}
                  </span>
                </div>
                <div className="flex justify-between text-xs">
                  <span className="text-slate-400">P&L</span>
                  <span className={parseFloat(profitPercent) >= 0
                    ? 'text-green-400' : 'text-red-400'}>
                    {profitPercent >= 0 ? '+' : ''}
                    {profitPercent}%
                  </span>
                </div>
              </div>

              {/* Progress bar */}
              <div className="mt-2 bg-slate-700 rounded-full h-1">
                <div
                  className={`h-1 rounded-full transition-all
                      ${parseFloat(profitPercent) >= 0
                        ? 'bg-green-500' : 'bg-red-500'}`}
                  style={{
                    width: `${Math.min(Math.abs(
                        parseFloat(profitPercent)) * 10, 100)}%`
                  }}
                />
              </div>
            </div>
          );
        })}

        {(!challenges || challenges.length === 0) && (
          <p className="text-slate-500 text-sm text-center py-4">
            No challenges yet. Start one!
          </p>
        )}
      </div>
    </div>
  );
}