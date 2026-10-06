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

```mermaid
flowchart TD
    A[React Frontend\nLogin · Signup · Dashboard · AI Coach\nPriceBar · TradingChart · TradePanel] 
    -->|HTTP Axios + JWT Bearer| B

    subgraph B[Spring Boot Backend :8080]
        C[JwtAuthenticationFilter\nValidates token → Sets SecurityContext]
        D[AuthController]
        E[TradeController]
        F[ChallengeController]
        G[MarketDataController]
        H[UserService]
        I[TradeService]
        J[ChallengeService]
        K[RuleEnforcementService\nDaily Loss · Drawdown · Profit Target\nConsistency · Best Day · Floating Loss]
        L[NewsCalendarService\nFinnhub API]
        M[MarketDataService\nConcurrentHashMap - 35+ instruments]
        N[PriceScheduler]

        C --> D & E & F & G
        D --> H
        E --> I
        F --> J
        I --> K
        K --> L
        G --> M
        N -->|every 5s| M
    end

    B --> DB[(PostgreSQL 18\nusers\nchallenge_rules\ntrading_accounts\nchallenge_attempts\ntrades)]

    N -->|crypto 5s| EX1[Binance REST]
    N -->|forex 30s| EX2[Frankfurter]
    N -->|metals 60s| EX3[gold-api.com]
    L -->|news events| EX4[Finnhub.io]
    A -->|AI Coach| EX5[Anthropic Claude API]
```

---

## Trade Flow

```mermaid
sequenceDiagram
    participant U as User
    participant FE as React Frontend
    participant JWT as JwtAuthFilter
    participant TS as TradeService
    participant MDS as MarketDataService
    participant RES as RuleEnforcementService
    participant DB as PostgreSQL

    U->>FE: Click BUY
    FE->>JWT: POST /api/trade/open + Bearer token
    JWT->>JWT: Validate token, extract email
    JWT->>TS: Forward request
    TS->>MDS: Get live price for symbol
    MDS-->>TS: Current market price
    TS->>DB: Save trade (status OPEN)
    
    U->>FE: Click CLOSE
    FE->>JWT: POST /api/trade/close?tradeId=X
    JWT->>TS: Forward request
    TS->>TS: Calculate P&L\n(Forex 100k · Gold 100oz · Crypto 1)
    TS->>DB: Update balance
    TS->>RES: checkRulesAfterTrade()
    RES->>RES: Daily Loss Limit?
    RES->>RES: Max Drawdown (STATIC or TRAILING)?
    RES->>RES: Profit Target + Min Days?
    RES->>RES: Consistency + Best Day Rule?
    RES->>DB: Update challenge PASSED / FAILED
    RES-->>FE: ruleCheckMessage + challengeStatus
```

---

## Security Flow

```mermaid
flowchart LR
    A["POST /auth/signup or /auth/login"] 
    --> B["BCrypt hash password"]
    --> C["Save to PostgreSQL"]
    --> D["Generate JWT - HS512 - 24h expiry"]
    --> E["Return token to client"]
    --> F["Client stores in localStorage"]

    G["Protected Request"] 
    --> H["Authorization: Bearer token"]
    --> I["JwtAuthenticationFilter"]
    --> J{"Token valid?"}
    J -->|Yes| K["Extract email from token"]
    J -->|No| L["403 Forbidden"]
    K --> M["Set in SecurityContext"]
    M --> N["SecurityUtils getCurrentUserEmail"]
    N --> O["Controller processes request - Email NEVER from URL params"]
```


## Database Schema

```mermaid
erDiagram
    users {
        bigint id PK
        varchar email
        varchar password
        varchar full_name
        varchar country
        decimal balance
        boolean kyc_verified
    }

    challenge_rules {
        bigint id PK
        varchar firm_name
        varchar challenge_type
        int phase_number
        decimal account_size
        decimal profit_target_percent
        decimal daily_loss_limit_percent
        decimal max_drawdown_percent
        varchar max_loss_type
        boolean news_trading_allowed
        decimal consistency_rule_percent
    }

    trading_accounts {
        bigint id PK
        bigint user_id FK
        decimal account_size
        decimal current_balance
        decimal starting_balance
        varchar status
    }

    challenge_attempts {
        bigint id PK
        bigint user_id FK
        bigint trading_account_id FK
        bigint challenge_rules_id FK
        varchar status
        bigint started_at
        bigint ended_at
    }

    trades {
        bigint id PK
        bigint user_id FK
        bigint trading_account_id FK
        bigint challenge_attempt_id FK
        varchar symbol
        varchar direction
        decimal lot_size
        decimal open_price
        decimal close_price
        decimal profit_loss
        varchar status
    }

    users ||--o{ trading_accounts : owns
    users ||--o{ challenge_attempts : attempts
    users ||--o{ trades : places
    trading_accounts ||--o{ challenge_attempts : used_in
    trading_accounts ||--o{ trades : contains
    challenge_rules ||--o{ challenge_attempts : governs
    challenge_attempts ||--o{ trades : tracks
```
