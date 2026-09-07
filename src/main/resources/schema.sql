CREATE TABLE IF NOT EXISTS financial_operation (
    id BIGSERIAL PRIMARY KEY,
    telegram_user_id BIGINT NOT NULL,
    concept VARCHAR(100) NOT NULL,
    amount NUMERIC(12, 2) NOT NULL,
    type VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT chk_financial_operation_amount_positive CHECK (amount > 0),
    CONSTRAINT chk_financial_operation_type CHECK (type IN ('EXPENSE', 'INCOME'))
);

CREATE INDEX IF NOT EXISTS idx_financial_operation_user_date
    ON financial_operation (telegram_user_id, created_at);
