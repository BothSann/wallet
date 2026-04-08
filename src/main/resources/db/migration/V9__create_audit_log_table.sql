CREATE TABLE audit_log (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entity_type VARCHAR(50)  NOT NULL,
    entity_id   UUID         NOT NULL,
    action      VARCHAR(50)  NOT NULL,
    actor_id    UUID,
    old_value   JSONB,
    new_value   JSONB,
    created_at  TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE INDEX idx_audit_log_entity     ON audit_log (entity_type, entity_id);
CREATE INDEX idx_audit_log_actor      ON audit_log (actor_id);
CREATE INDEX idx_audit_log_created_at ON audit_log (created_at DESC);
