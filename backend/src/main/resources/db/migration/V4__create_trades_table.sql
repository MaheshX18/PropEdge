CREATE TABLE IF NOT EXISTS trades (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    trading_account_id BIGINT NOT NULL REFERENCES trading_accounts(id),
    challenge_attempt_id BIGINT NOT NULL REFERENCES challenge_attempts(id),
    symbol VARCHAR(20) NOT NULL,
    direction VARCHAR(10) NOT NULL,
    lot_size DECIMAL(10,2) NOT NULL,
    open_price DECIMAL(15,5) NOT NULL,
    close_price DECIMAL(15,5),
    stop_loss DECIMAL(15,5),
    take_profit DECIMAL(15,5),
    profit_loss DECIMAL(15,2),
    status VARCHAR(10) NOT NULL DEFAULT 'OPEN',
    opened_at BIGINT NOT NULL,
    closed_at BIGINT,
    created_at BIGINT NOT NULL
);

CREATE INDEX idx_trades_user_id ON trades(user_id);
CREATE INDEX idx_trades_trading_account_id ON trades(trading_account_id);
CREATE INDEX idx_trades_status ON trades(status);
CREATE INDEX idx_trades_symbol ON trades(symbol);