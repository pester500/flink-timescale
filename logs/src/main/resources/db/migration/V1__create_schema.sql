CREATE TABLE IF NOT EXISTS logs(
    filename TEXT NOT NULL,
    word TEXT NOT NULL,
    count TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL
);

ALTER TABLE logs ADD PRIMARY KEY (filename, word);

CREATE TABLE IF NOT EXISTS failures(
  failure TEXT NOT NULL,
  created_at TIMESTAMP NOT NULL
);

SELECT create_hypertable('logs', 'created_at');
