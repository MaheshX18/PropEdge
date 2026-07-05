import { useState } from 'react';
import axios from 'axios';

export default function AICoach({ challenges, openTrades }) {
  const [messages, setMessages] = useState([
    {
      role: 'assistant',
      content: `Hi! I'm your AI trading coach powered by Claude.
I can analyze your trades, explain prop firm rules,
and help you pass your challenge. What would you like to know?`
    }
  ]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);

  const sendMessage = async () => {
    if (!input.trim() || loading) return;

    const userMessage = { role: 'user', content: input };
    const newMessages = [...messages, userMessage];
    setMessages(newMessages);
    setInput('');
    setLoading(true);

    try {
      // Build context about user's trading
      const context = `
You are an expert prop firm trading coach helping a trader
practice for real prop firm challenges.

Current trading context:
- Active challenges: ${challenges?.filter(
    c => c.status === 'ACTIVE').length || 0}
- Open trades: ${openTrades?.length || 0}
${openTrades?.length > 0 ? `- Current open positions: ${
  openTrades.map(t =>
    `${t.direction} ${t.symbol} ${t.lotSize} lots`
  ).join(', ')}` : ''}

Help the trader with prop firm rules, trading strategy,
risk management, and passing their challenge.
Be concise, practical and encouraging.`;

      const response = await axios.post(
        'https://api.anthropic.com/v1/messages',
        {
          model: 'claude-sonnet-4-6',
          max_tokens: 1000,
          system: context,
          messages: newMessages.map(m => ({
            role: m.role,
            content: m.content
          }))
        },
        {
          headers: {
            'Content-Type': 'application/json',
          }
        }
      );

      const aiMessage = {
        role: 'assistant',
        content: response.data.content[0].text
      };
      setMessages([...newMessages, aiMessage]);
    } catch (err) {
      setMessages([...newMessages, {
        role: 'assistant',
        content: 'Sorry, I encountered an error. Please try again!'
      }]);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex flex-col h-full">
      <h3 className="text-white font-semibold mb-3 flex items-center gap-2">
        🤖 AI Trading Coach
      </h3>

      {/* Messages */}
      <div className="flex-1 overflow-y-auto space-y-3 mb-3
          max-h-80">
        {messages.map((msg, idx) => (
          <div key={idx} className={`flex
              ${msg.role === 'user'
                ? 'justify-end' : 'justify-start'}`}>
            <div className={`max-w-xs px-3 py-2 rounded-lg text-sm
                ${msg.role === 'user'
                  ? 'bg-blue-600 text-white'
                  : 'bg-slate-700 text-slate-200'}`}>
              {msg.content}
            </div>
          </div>
        ))}
        {loading && (
          <div className="flex justify-start">
            <div className="bg-slate-700 px-3 py-2 rounded-lg
                text-slate-400 text-sm">
              Thinking...
            </div>
          </div>
        )}
      </div>

      {/* Input */}
      <div className="flex gap-2">
        <input
          type="text"
          value={input}
          onChange={(e) => setInput(e.target.value)}
          onKeyPress={(e) => e.key === 'Enter' && sendMessage()}
          placeholder="Ask about rules, strategy..."
          className="flex-1 bg-slate-700 text-white rounded-lg
              px-3 py-2 text-sm border border-slate-600
              focus:border-blue-500 focus:outline-none"
        />
        <button
          onClick={sendMessage}
          disabled={loading}
          className="bg-blue-600 hover:bg-blue-700 text-white
              px-3 py-2 rounded-lg text-sm transition-colors
              disabled:opacity-50"
        >
          Send
        </button>
      </div>
    </div>
  );
}