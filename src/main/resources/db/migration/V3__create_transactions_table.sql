CREATE TABLE transactions
(
    id               UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    wallet_id        UUID           NOT NULL REFERENCES wallets (id) ON DELETE CASCADE,
    idempotency_key  VARCHAR(255)   NOT NULL UNIQUE,
    type             VARCHAR(30)    NOT NULL,
    status           VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    amount           NUMERIC(19, 4) NOT NULL,
    balance_before   NUMERIC(19, 4) NOT NULL,
    balance_after    NUMERIC(19, 4) NOT NULL,
    description      TEXT,
    created_at       TIMESTAMP      NOT NULL DEFAULT now()
);

CREATE INDEX idx_transactions_wallet_id  ON transactions (wallet_id);
CREATE INDEX idx_transactions_created_at ON transactions (created_at DESC);
CREATE INDEX idx_transactions_type       ON transactions (type);
