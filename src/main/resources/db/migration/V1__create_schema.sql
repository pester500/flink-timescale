CREATE TABLE IF NOT EXISTS words(
    word VARCHAR(256) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

SELECT create_hypertable('words', 'created_at', 'word', 4);
