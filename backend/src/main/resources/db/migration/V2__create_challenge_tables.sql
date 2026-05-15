-- Table 1: Challenge Rules (firm rules like FTMO, FundingPips)
CREATE TABLE IF NOT EXISTS challenge_rules (
    id BIGSERIAL PRIMARY KEY,
    firm_name VARCHAR(100) NOT NULL,
    account_size DECIMAL(15,2) NOT NULL,
    profit_target_percent DECIMAL(5,2) NOT NULL,
    max_drawdown_percent DECIMAL(5,2) NOT NULL,
    daily_loss_limit_percent DECIMAL(5,2) NOT NULL,
    max_trading_days INTEGER NOT NULL,
    min_trading_days INTEGER NOT NULL DEFAULT 0,
    created_at BIGINT NOT NULL
);

-- Table 2: Trading Accounts (created when user starts challenge)
CREATE TABLE IF NOT EXISTS trading_accounts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    account_size DECIMAL(15,2) NOT NULL,
    current_balance DECIMAL(15,2) NOT NULL,
    starting_balance DECIMAL(15,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at BIGINT NOT NULL,
    updated_at BIGINT
);

-- Table 3: Challenge Attempts (links user + account + rules)
CREATE TABLE IF NOT EXISTS challenge_attempts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    trading_account_id BIGINT NOT NULL REFERENCES trading_accounts(id),
    challenge_rules_id BIGINT NOT NULL REFERENCES challenge_rules(id),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    started_at BIGINT NOT NULL,
    ended_at BIGINT,
    created_at BIGINT NOT NULL
);

-- Indexes for fast lookup
CREATE INDEX idx_trading_accounts_user_id ON trading_accounts(user_id);
CREATE INDEX idx_challenge_attempts_user_id ON challenge_attempts(user_id);
CREATE INDEX idx_challenge_attempts_status ON challenge_attempts(status);

-- Insert default challenge rules (FTMO style)
INSERT INTO challenge_rules (firm_name, account_size, profit_target_percent, max_drawdown_percent, daily_loss_limit_percent, max_trading_days, min_trading_days, created_at) VALUES
('PRACTICE', 5000.00, 10.00, 10.00, 5.00, 30, 0, EXTRACT(EPOCH FROM NOW())::BIGINT),
('PRACTICE', 10000.00, 10.00, 10.00, 5.00, 30, 0, EXTRACT(EPOCH FROM NOW())::BIGINT),
('PRACTICE', 25000.00, 10.00, 10.00, 5.00, 30, 0, EXTRACT(EPOCH FROM NOW())::BIGINT),
('PRACTICE', 50000.00, 10.00, 10.00, 5.00, 30, 0, EXTRACT(EPOCH FROM NOW())::BIGINT),
('PRACTICE', 100000.00, 10.00, 10.00, 5.00, 30, 0, EXTRACT(EPOCH FROM NOW())::BIGINT);