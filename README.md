# PropPractice — Prop Firm Trading Challenge Platform

> Practice real prop firm challenges at 100 INR instead of $40–200

A full-stack paper trading platform that simulates real prop firm challenges (FTMO, FundingPips, The5ers, and 9 more) using live market prices but paper money. Built with Java Spring Boot backend and React frontend.

---

## What It Does

Traders pay 100 INR per challenge attempt and get a simulated trading account with the exact same rules as real prop firms. The platform streams real market prices, tracks every trade, and automatically passes or fails the challenge based on the firm's official rules — daily loss limits, max drawdown, profit targets, consistency rules, and more.

---

## Features

- Live prices for 35+ instruments (forex, crypto, gold, silver, oil, indices)
- Paper trading with real P&L calculation per instrument type
- 12 prop firms with 200+ verified challenge configurations
- Rule enforcement engine — auto PASS/FAIL after every trade
- News trading detection via Finnhub Economic Calendar
- JWT authentication with BCrypt password hashing
- AI trading coach powered by Claude (Anthropic)
- Clean REST API with consistent ApiResponse<T> format

---

## Supported Prop Firms

FTMO · FundingPips · Funded Room · Funded Firm · FundedNext · Funded Friday · Blueberry Funded · Goat Funded Trader · The5ers · Alpha Capital · E8 Markets · Maven Trading

---

## Tech Stack

### Backend
- Java 17
- Spring Boot 4.0.6
- Spring Security (JWT, stateless)
- Spring Data JPA + Hibernate
- Flyway (database migrations)
- Spring WebFlux (WebClient for async API calls)
- PostgreSQL 18
- jjwt 0.12.3
- spring-dotenv 4.0.0
- Lombok

### Frontend
- React 18 + Vite
- TanStack React Query
- Axios (with JWT interceptors)
- TradingView Lightweight Charts
- Tailwind CSS v4
- React Router DOM

### Market Data APIs
| Data | Provider | Cost |
|------|----------|------|
| Crypto (7 pairs) | Binance REST | Free |
| Forex (18 pairs) | Frankfurter | Free |
| Gold + Silver | gold-api.com | Free |
| Economic Calendar | Finnhub.io | Free (key needed) |

---

## Architecture
┌─────────────────────────────────────────────────────┐
│ React Frontend │
│ Login │ Dashboard │ TradingChart │ AI Coach │
└─────────────────────┬───────────────────────────────┘
│ HTTP (Axios + JWT Bearer)
▼
┌─────────────────────────────────────────────────────┐
│ Spring Boot Backend (:8080) │
│ │
│ JwtAuthenticationFilter → SecurityContext │
│ │
│ AuthController → UserService │
│ ChallengeController → ChallengeService │
│ TradeController → TradeService │
│ └→ RuleEnforcementService │
│ └→ NewsCalendarService │
│ MarketDataController → MarketDataService │
│ │
│ PriceScheduler (every 5s/30s/60s) │
└──────────┬──────────────────────────┬───────────────┘
│ │
▼ ▼
┌──────────────────┐ ┌──────────────────────────┐
│ PostgreSQL 18 │ │ External APIs │
│ │ │ │
│ users │ │ Binance → crypto │
│ challenge_rules │ │ Frankfurter → forex │
│ trading_accounts│ │ gold-api.com → metals │
│ challenge_ │ │ Finnhub → news calendar │
│ attempts │ │ Anthropic → AI Coach │
│ trades │ │ │
└──────────────────┘ └──────────────────────────┘


### Request Flow

User places trade
↓
POST /api/trade/open
↓
JwtAuthenticationFilter validates token
↓
Email extracted from SecurityContext (never from URL)
↓
TradeService fetches live price from MarketDataService cache
↓
Trade saved to PostgreSQL
↓
POST /api/trade/close
↓
P&L calculated (instrument-aware contract size)
↓
Balance updated in TradingAccount
↓
RuleEnforcementService runs all checks:
→ Daily loss limit (static % of starting balance)
→ Max drawdown (STATIC or TRAILING depending on firm)
→ Floating loss limit
→ Profit target + min trading days
→ Consistency rule (best day % of total)
→ News trading violation (Finnhub calendar check)
↓
Challenge auto-updated: ACTIVE → PASSED or FAILED
↓
Response includes: ruleCheckMessage, violationType, challengeStatus


### P&L Calculation by Instrument

Forex (EURUSD etc.) → contractSize = 100,000 units/lot
Gold (XAUUSD) → contractSize = 100 oz/lot
Silver (XAGUSD) → contractSize = 5,000 oz/lot
Oil (USOIL/UKOIL) → contractSize = 1,000 barrels/lot
Crypto (BTCUSDT etc) → contractSize = 1

P&L = priceDifference × lotSize × contractSize


---

## Database Schema

users
└── id, email, password (BCrypt), full_name, country, balance, kyc_verified

challenge_rules (200+ rows — 12 firms × account types × sizes)
└── firm_name, challenge_type, phase_number, account_size
profit_target_percent, daily_loss_limit_percent, max_drawdown_percent
max_loss_type (STATIC/TRAILING/EOD), news_trading_allowed
weekend_holding_allowed, consistency_rule_percent, best_day_rule_percent
leverage_forex, leverage_crypto, leverage_metals ... (30+ columns)

trading_accounts
└── user_id (FK), account_size, current_balance, starting_balance, status

challenge_attempts
└── user_id (FK), trading_account_id (FK), challenge_rules_id (FK)
status (ACTIVE/PASSED/FAILED), started_at, ended_at

trades
└── user_id (FK), trading_account_id (FK), challenge_attempt_id (FK)
symbol, direction (BUY/SELL), lot_size, open_price, close_price
stop_loss, take_profit, profit_loss, status (OPEN/CLOSED)


---

## API Endpoints

### Public (No Auth Required)

POST /api/auth/signup Register new user
POST /api/auth/login Login, receive JWT token
GET /api/market/prices All live prices (35+ instruments)
GET /api/market/price/{symbol} Single instrument price
GET /api/challenge/rules All prop firm rules


### Protected (JWT Bearer Token Required)

POST /api/challenge/start Start new challenge attempt
GET /api/challenge/my-challenges Get user's challenges
POST /api/trade/open Open a trade at live market price
POST /api/trade/close?tradeId=X Close trade + run rule enforcement
GET /api/trade/open-trades List open trades
GET /api/trade/all-trades List all trades


### Standard Response Format
```json
{
  "success": true,
  "message": "Trade opened successfully",
  "data": { ... },
  "timestamp": "2026-07-01T10:00:00.000Z"
}
```

---

## Getting Started

### Prerequisites
- Java 17+
- Maven 3.9+
- PostgreSQL 18
- Node.js 18+
- npm

### 1. Clone the Repository
```bash
git clone https://github.com/MaheshX18/MVP.git
cd trading_challenge_platform
```

### 2. Set Up PostgreSQL
```bash
psql -U postgres
CREATE DATABASE trading_challenge;
\q
```

### 3. Configure Environment Variables
Create a file at `backend/.env`:

DB_URL=jdbc:postgresql://localhost:5432/trading_challenge
DB_USERNAME=postgres
DB_PASSWORD=your_postgres_password
JWT_SECRET=your_minimum_256_bit_secret_key_here_make_it_long
JWT_EXPIRATION=86400000
FINNHUB_API_KEY=your_finnhub_api_key
POLYGON_API_KEY=optional_not_actively_used
ALPHAVANTAGE_API_KEY=optional_not_actively_used


Get a free Finnhub API key at: https://finnhub.io

### 4. Run the Backend
```bash
cd backend
mvn spring-boot:run
```
Wait for:

Tomcat started on port 8080 (http) with context path '/api'


Flyway will automatically create all tables and seed prop firm rules on first run.

### 5. Run the Frontend
Open a new terminal:
```bash
cd frontend
npm install
npm run dev
```

Open browser at: http://localhost:5173

The frontend proxies all `/api` requests to `http://localhost:8080` automatically.

---

## Environment Variables Reference

| Variable | Required | Description |
|----------|----------|-------------|
| DB_URL | Yes | PostgreSQL connection URL |
| DB_USERNAME | Yes | PostgreSQL username |
| DB_PASSWORD | Yes | PostgreSQL password |
| JWT_SECRET | Yes | Min 256-bit string for JWT signing |
| JWT_EXPIRATION | Yes | Token expiry in ms (86400000 = 24h) |
| FINNHUB_API_KEY | Yes | For news trading detection |
| POLYGON_API_KEY | No | Not actively used in current version |
| ALPHAVANTAGE_API_KEY | No | Not actively used in current version |

---

## Project Structure

trading_challenge_platform/
├── backend/
│ ├── src/main/java/com/mvp18/trading_challenge_backend/
│ │ ├── controller/ AuthController, ChallengeController,
│ │ │ TradeController, MarketDataController
│ │ ├── service/ UserService, ChallengeService, TradeService,
│ │ │ MarketDataService, PriceScheduler,
│ │ │ RuleEnforcementService, NewsCalendarService
│ │ │ └── interfaces/ IUserService, IChallengeService,
│ │ │ ITradeService, IMarketDataService
│ │ ├── repository/ UserRepository, TradeRepository, ...
│ │ ├── dto/ ApiResponse, AuthResponse, TradeResponse,
│ │ │ OpenTradeRequest, ChallengeResponse
│ │ ├── exception/ GlobalExceptionHandler, custom exceptions
│ │ ├── security/ JwtAuthenticationFilter, SecurityUtils
│ │ └── util/ JwtUtil
│ └── src/main/resources/
│ ├── application.properties
│ └── db/migration/ V1 through V5 Flyway SQL scripts
├── frontend/
│ └── src/
│ ├── components/ TradingChart, TradePanel, ChallengeStatus,
│ │ PriceBar, AICoach
│ ├── pages/ Login, Signup, Dashboard
│ ├── context/ AuthContext
│ └── services/ api.js (Axios + JWT interceptor)
└── PROJECT_STATE.md


---

## Roadmap

- [x] JWT authentication + BCrypt
- [x] Real-time market data (35+ instruments)
- [x] Paper trading engine with instrument-aware P&L
- [x] 12 prop firms with verified official rules
- [x] Rule enforcement engine (all major rules)
- [x] News trading detection (Finnhub)
- [x] React frontend with TradingView charts
- [x] AI trading coach (Claude API)
- [ ] Razorpay payment integration (100 INR per challenge)
- [ ] Chart timeframe selector (1m, 5m, 15m, 1h, 4h, 1D)
- [ ] Trade history and analytics dashboard
- [ ] Rate limiting and refresh tokens
- [ ] Docker + AWS/GCP deployment
- [ ] Email verification
- [ ] WebSocket for real-time price streaming
- [ ] Mobile responsive design

---

## Contributing

This is currently a solo project. PRs and suggestions are welcome.

---

## License

MIT License — feel free to use, modify, and distribute.

---

## Author

**Mahesh Patil**
GitHub: https://github.com/MaheshX18

Copy everything between the triple backticks and paste it directly into a new file called README.md in your project root. Then:

bash
git add README.md
git commit -m "docs: Add comprehensive README"
git push
