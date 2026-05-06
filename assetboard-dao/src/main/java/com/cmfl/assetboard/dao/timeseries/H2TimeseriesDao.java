package com.cmfl.assetboard.dao.timeseries;

import com.cmfl.assetboard.common.kv.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@Repository
public class H2TimeseriesDao implements TimeseriesDao {

    private final JdbcTemplate jdbc;

    public H2TimeseriesDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    @Transactional
    public void save(UUID entityId, TsKvEntry entry) {
        int keyId = getOrCreateKeyId(entry.getKey());
        Boolean boolV = null;
        String strV = null;
        Long longV = null;
        Double dblV = null;
        String jsonV = null;
        switch (entry.getDataType()) {
            case BOOLEAN -> boolV = (Boolean) entry.getValue();
            case LONG -> longV = (Long) entry.getValue();
            case DOUBLE -> dblV = (Double) entry.getValue();
            case STRING -> strV = (String) entry.getValue();
            case JSON -> jsonV = (String) entry.getValue();
        }
        jdbc.update("MERGE INTO ts_kv (entity_id, key_id, ts, bool_v, str_v, long_v, dbl_v, json_v) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                entityId, keyId, entry.getTs(), boolV, strV, longV, dblV, jsonV);
        jdbc.update("MERGE INTO ts_kv_latest (entity_id, key_id, ts, bool_v, str_v, long_v, dbl_v, json_v) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                entityId, keyId, entry.getTs(), boolV, strV, longV, dblV, jsonV);
    }

    @Override
    @Transactional
    public void saveBatch(UUID entityId, List<TsKvEntry> entries) {
        for (TsKvEntry entry : entries) {
            save(entityId, entry);
        }
    }

    @Override
    public List<TsKvEntry> find(UUID entityId, String key, long startTs, long endTs, int limit) {
        int keyId = findKeyId(key);
        if (keyId < 0) return List.of();
        return jdbc.query(
                "SELECT ts, bool_v, str_v, long_v, dbl_v, json_v FROM ts_kv WHERE entity_id = ? AND key_id = ? AND ts >= ? AND ts <= ? ORDER BY ts DESC LIMIT ?",
                (rs, rowNum) -> mapTsKvEntry(rs, key),
                entityId, keyId, startTs, endTs, limit);
    }

    @Override
    public TsKvEntry findLatest(UUID entityId, String key) {
        int keyId = findKeyId(key);
        if (keyId < 0) return null;
        List<TsKvEntry> results = jdbc.query(
                "SELECT ts, bool_v, str_v, long_v, dbl_v, json_v FROM ts_kv_latest WHERE entity_id = ? AND key_id = ?",
                (rs, rowNum) -> mapTsKvEntry(rs, key),
                entityId, keyId);
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public List<TsKvEntry> findAllLatest(UUID entityId) {
        return jdbc.query(
                "SELECT l.ts, l.bool_v, l.str_v, l.long_v, l.dbl_v, l.json_v, d.key_name FROM ts_kv_latest l JOIN key_dictionary d ON l.key_id = d.key_id WHERE l.entity_id = ?",
                (rs, rowNum) -> mapTsKvEntry(rs, rs.getString("key_name")),
                entityId);
    }

    @Override
    @Transactional
    public void delete(UUID entityId, String key, long startTs, long endTs) {
        int keyId = findKeyId(key);
        if (keyId < 0) return;
        jdbc.update("DELETE FROM ts_kv WHERE entity_id = ? AND key_id = ? AND ts >= ? AND ts <= ?",
                entityId, keyId, startTs, endTs);
    }

    private int getOrCreateKeyId(String key) {
        List<Integer> ids = jdbc.queryForList("SELECT key_id FROM key_dictionary WHERE key_name = ?", Integer.class, key);
        if (!ids.isEmpty()) return ids.get(0);
        jdbc.update("INSERT INTO key_dictionary (key_name) VALUES (?)", key);
        return jdbc.queryForObject("SELECT key_id FROM key_dictionary WHERE key_name = ?", Integer.class, key);
    }

    private int findKeyId(String key) {
        List<Integer> ids = jdbc.queryForList("SELECT key_id FROM key_dictionary WHERE key_name = ?", Integer.class, key);
        return ids.isEmpty() ? -1 : ids.get(0);
    }

    private TsKvEntry mapTsKvEntry(ResultSet rs, String key) throws SQLException {
        long ts = rs.getLong("ts");
        KvEntry kv;
        if (rs.getObject("bool_v") != null) {
            kv = BasicKvEntry.ofBoolean(key, rs.getBoolean("bool_v"));
        } else if (rs.getObject("long_v") != null) {
            kv = BasicKvEntry.ofLong(key, rs.getLong("long_v"));
        } else if (rs.getObject("dbl_v") != null) {
            kv = BasicKvEntry.ofDouble(key, rs.getDouble("dbl_v"));
        } else if (rs.getString("json_v") != null) {
            kv = BasicKvEntry.ofJson(key, rs.getString("json_v"));
        } else {
            kv = BasicKvEntry.ofString(key, rs.getString("str_v"));
        }
        return new BasicTsKvEntry(ts, kv);
    }
}
