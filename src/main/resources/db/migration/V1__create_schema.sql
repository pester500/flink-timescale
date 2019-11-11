CREATE TABLE IF NOT EXISTS crimes(
    id INTEGER NOT NULL,
    case_number TEXT NOT NULL,
    date TIMESTAMP NOT NULL,
    block TEXT NOT NULL,
    iucr TEXT NOT NULL,
    primary_type TEXT NOT NULL,
    description TEXT NOT NULL,
    location_description TEXT NOT NULL,
    arrest BOOLEAN,
    domestic BOOLEAN,
    beat TEXT,
    district TEXT,
    ward SMALLINT,
    community_area TEXT,
    fbi_code TEXT,
    x_coordinate FLOAT,
    y_coordinate FLOAT,
    year INTEGER,
    updated_on TIMESTAMP,
    latitude FLOAT,
    longitude FLOAT,
    location TEXT,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS failures(
  failure TEXT NOT NULL,
  created_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS processing_speed(
 district VARCHAR NOT NULL,
 count INTEGER NOT NULL,
 window_begin TIMESTAMP NOT NULL
);

SELECT create_hypertable('crimes', 'created_at');
