CREATE TABLE IF NOT EXISTS key_dictionary (
    key_id INT AUTO_INCREMENT PRIMARY KEY,
    key_name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS attribute_kv (
    entity_id UUID NOT NULL,
    attribute_key INT NOT NULL,
    bool_v BOOLEAN,
    str_v CLOB,
    long_v BIGINT,
    dbl_v DOUBLE,
    json_v CLOB,
    last_update_ts BIGINT NOT NULL,
    PRIMARY KEY (entity_id, attribute_key)
);

CREATE TABLE IF NOT EXISTS ts_kv (
    entity_id UUID NOT NULL,
    key_id INT NOT NULL,
    ts BIGINT NOT NULL,
    bool_v BOOLEAN,
    str_v CLOB,
    long_v BIGINT,
    dbl_v DOUBLE,
    json_v CLOB,
    PRIMARY KEY (entity_id, key_id, ts)
);
CREATE INDEX IF NOT EXISTS idx_ts_kv_ts ON ts_kv(entity_id, key_id, ts DESC);

CREATE TABLE IF NOT EXISTS ts_kv_latest (
    entity_id UUID NOT NULL,
    key_id INT NOT NULL,
    ts BIGINT NOT NULL,
    bool_v BOOLEAN,
    str_v CLOB,
    long_v BIGINT,
    dbl_v DOUBLE,
    json_v CLOB,
    PRIMARY KEY (entity_id, key_id)
);
