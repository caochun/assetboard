CREATE TABLE IF NOT EXISTS tenant (
    id UUID PRIMARY KEY,
    created_time BIGINT,
    title VARCHAR(255) NOT NULL,
    region VARCHAR(255),
    country VARCHAR(255),
    city VARCHAR(255),
    address VARCHAR(1000),
    phone VARCHAR(64),
    email VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS asset_profile (
    id UUID PRIMARY KEY,
    created_time BIGINT,
    tenant_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    image CLOB,
    CONSTRAINT uk_asset_profile_name UNIQUE (tenant_id, name)
);

CREATE TABLE IF NOT EXISTS customer (
    id UUID PRIMARY KEY,
    created_time BIGINT,
    tenant_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    credit_amount DECIMAL(18,2),
    remaining_principal DECIMAL(18,2),
    address CLOB,
    contact_info CLOB,
    additional_info CLOB,
    CONSTRAINT uk_customer_name UNIQUE (tenant_id, name)
);

CREATE TABLE IF NOT EXISTS project (
    id UUID PRIMARY KEY,
    created_time BIGINT,
    tenant_id UUID NOT NULL,
    customer_id UUID REFERENCES customer(id),
    name VARCHAR(255) NOT NULL,
    project_no VARCHAR(64),
    business_type VARCHAR(64),
    lease_type VARCHAR(64),
    additional_info CLOB
);

CREATE TABLE IF NOT EXISTS contract (
    id UUID PRIMARY KEY,
    created_time BIGINT,
    project_id UUID REFERENCES project(id),
    contract_no VARCHAR(64) NOT NULL,
    amount DECIMAL(18,2),
    currency VARCHAR(16) DEFAULT 'CNY',
    lessor VARCHAR(255),
    lessee VARCHAR(255),
    status VARCHAR(32),
    sign_date BIGINT,
    additional_info CLOB
);

CREATE TABLE IF NOT EXISTS asset (
    id UUID PRIMARY KEY,
    created_time BIGINT,
    tenant_id UUID NOT NULL,
    customer_id UUID,
    asset_profile_id UUID NOT NULL REFERENCES asset_profile(id),
    name VARCHAR(255) NOT NULL,
    type VARCHAR(255),
    label VARCHAR(255),
    status VARCHAR(32) DEFAULT 'IN_LEASE',
    additional_info CLOB,
    CONSTRAINT uk_asset_name UNIQUE (tenant_id, name)
);

CREATE TABLE IF NOT EXISTS alarm (
    id UUID PRIMARY KEY,
    created_time BIGINT,
    tenant_id UUID NOT NULL,
    originator_id UUID NOT NULL,
    originator_type INT NOT NULL,
    type VARCHAR(255) NOT NULL,
    severity VARCHAR(32) NOT NULL,
    acknowledged BOOLEAN DEFAULT FALSE,
    cleared BOOLEAN DEFAULT FALSE,
    start_ts BIGINT,
    end_ts BIGINT,
    ack_ts BIGINT,
    clear_ts BIGINT,
    details CLOB
);
CREATE INDEX IF NOT EXISTS idx_alarm_originator ON alarm(originator_id, originator_type);
CREATE INDEX IF NOT EXISTS idx_alarm_tenant ON alarm(tenant_id, created_time DESC);

CREATE TABLE IF NOT EXISTS tb_user (
    id UUID PRIMARY KEY,
    created_time BIGINT,
    tenant_id UUID NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    authority VARCHAR(32) NOT NULL,
    name VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS alarm_rule (
    id UUID PRIMARY KEY,
    created_time BIGINT,
    tenant_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    target_type VARCHAR(255),
    telemetry_key VARCHAR(255) NOT NULL,
    condition VARCHAR(32) NOT NULL,
    threshold DOUBLE NOT NULL,
    severity VARCHAR(32) NOT NULL,
    alarm_type VARCHAR(255) NOT NULL,
    enabled BOOLEAN DEFAULT TRUE
);
CREATE INDEX IF NOT EXISTS idx_alarm_rule_tenant ON alarm_rule(tenant_id);
CREATE INDEX IF NOT EXISTS idx_alarm_rule_key ON alarm_rule(enabled, telemetry_key);

CREATE TABLE IF NOT EXISTS entity_relation (
    from_id UUID NOT NULL,
    from_type INT NOT NULL,
    to_id UUID NOT NULL,
    to_type INT NOT NULL,
    relation_type VARCHAR(255) NOT NULL,
    additional_info CLOB,
    PRIMARY KEY (from_id, from_type, to_id, to_type, relation_type)
);

CREATE TABLE IF NOT EXISTS data_source_config (
    id UUID PRIMARY KEY,
    created_time BIGINT,
    asset_id UUID NOT NULL,
    collector_id VARCHAR(64) NOT NULL,
    enabled BOOLEAN DEFAULT TRUE,
    params CLOB,
    CONSTRAINT uk_dsc UNIQUE (asset_id, collector_id)
);
