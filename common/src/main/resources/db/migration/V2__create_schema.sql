CREATE TABLE IF NOT EXISTS logs(
    id BIGSERIAL,
    line TEXT NOT NULL,
    ingested TIMESTAMP NOT NULL,
    processed TIMESTAMP NOT NULL,
    length INTEGER NOT NULL,
    source TEXT NOT NULL,
    character_map TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS log_failures(
  failure TEXT NOT NULL,
  created_at TIMESTAMP NOT NULL
);

CREATE INDEX ON logs (id,   processed);
SELECT create_hypertable('logs', 'processed')