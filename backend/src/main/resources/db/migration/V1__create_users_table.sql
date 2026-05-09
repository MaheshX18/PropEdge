CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    country VARCHAR(100),
    balance DECIMAL(15,2) DEFAULT 0.00,
    kyc_verified BOOLEAN DEFAULT false,
    created_at BIGINT NOT NULL,
    updated_at BIGINT,
    created_at_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at_timestamp TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_created_at ON users(created_at);