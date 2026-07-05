const SYMBOLS = [
  'BTCUSDT', 'ETHUSDT', 'SOLUSDT', 'BNBUSDT',
  'EURUSD', 'GBPUSD', 'USDJPY', 'XAUUSD',
  'XAGUSD', 'USOIL'
];

export default function PriceBar({
  prices, selectedSymbol, onSelectSymbol
}) {
  return (
    <div className="bg-slate-800 border-b border-slate-700
        px-4 py-2 flex gap-4 overflow-x-auto">
      {SYMBOLS.map(symbol => {
        const price = prices?.[symbol] || 0;
        const isSelected = symbol === selectedSymbol;

        return (
          <button
            key={symbol}
            onClick={() => onSelectSymbol(symbol)}
            className={`flex items-center gap-2 px-3 py-1
                rounded-lg whitespace-nowrap transition-colors
                ${isSelected
                  ? 'bg-blue-600 text-white'
                  : 'text-slate-400 hover:text-white'}`}
          >
            <span className="text-xs font-semibold">
              {symbol}
            </span>
            <span className="text-xs">
              {price > 100
                ? price.toFixed(2)
                : price.toFixed(5)}
            </span>
          </button>
        );
      })}
    </div>
  );
}