-- V5__drift_features.sql
-- Add support for scheduled rides, review moderation, support ticket resolution notes, and persistent audit logs.

-- 1. Scheduled rides support
ALTER TABLE rides ADD COLUMN IF NOT EXISTS scheduled_for TIMESTAMP WITH TIME ZONE;
ALTER TABLE rides ALTER COLUMN currency SET DEFAULT 'INR';

-- 2. Support ticket resolution notes
ALTER TABLE support_tickets ADD COLUMN IF NOT EXISTS resolution_notes TEXT;

-- 3. Review moderation columns on ratings table
ALTER TABLE ratings ADD COLUMN IF NOT EXISTS is_hidden BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE ratings ADD COLUMN IF NOT EXISTS moderated_by BIGINT REFERENCES users(id);
ALTER TABLE ratings ADD COLUMN IF NOT EXISTS moderated_at TIMESTAMP WITH TIME ZONE;

-- 4. Persistent Audit Logs table
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGSERIAL PRIMARY KEY,
    actor_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    actor_email VARCHAR(100),
    actor_role VARCHAR(30),
    action VARCHAR(100) NOT NULL,
    resource_type VARCHAR(50) NOT NULL,
    resource_id VARCHAR(100),
    details TEXT,
    ip_address VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_audit_logs_actor ON audit_logs(actor_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_action ON audit_logs(action);
CREATE INDEX IF NOT EXISTS idx_audit_logs_created_at ON audit_logs(created_at);
