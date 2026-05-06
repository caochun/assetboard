package com.cmfl.assetboard.dao.attributes;

import com.cmfl.assetboard.common.kv.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class H2AttributesDao implements AttributesDao {

    private final JdbcTemplate jdbc;

    public H2AttributesDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    @Transactional
    public void save(UUID entityId, AttributeKvEntry entry) {
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
        jdbc.update("MERGE INTO attribute_kv (entity_id, attribute_key, bool_v, str_v, long_v, dbl_v, json_v, last_update_ts) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                entityId, keyId, boolV, strV, longV, dblV, jsonV, entry.getLastUpdateTs());
    }

    @Override
    public Optional<AttributeKvEntry> find(UUID entityId, String key) {
        int keyId = findKeyId(key);
        if (keyId < 0) return Optional.empty();
        List<AttributeKvEntry> results = jdbc.query(
                "SELECT bool_v, str_v, long_v, dbl_v, json_v, last_update_ts FROM attribute_kv WHERE entity_id = ? AND attribute_key = ?",
                (rs, rowNum) -> mapAttributeKvEntry(rs, key),
                entityId, keyId);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public List<AttributeKvEntry> findAll(UUID entityId) {
        return jdbc.query(
                "SELECT a.bool_v, a.str_v, a.long_v, a.dbl_v, a.json_v, a.last_update_ts, d.key_name FROM attribute_kv a JOIN key_dictionary d ON a.attribute_key = d.key_id WHERE a.entity_id = ?",
                (rs, rowNum) -> mapAttributeKvEntry(rs, rs.getString("key_name")),
                entityId);
    }

    @Override
    @Transactional
    public void delete(UUID entityId, String key) {
        int keyId = findKeyId(key);
        if (keyId < 0) return;
        jdbc.update("DELETE FROM attribute_kv WHERE entity_id = ? AND attribute_key = ?", entityId, keyId);
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

    private AttributeKvEntry mapAttributeKvEntry(ResultSet rs, String key) throws SQLException {
        long lastUpdateTs = rs.getLong("last_update_ts");
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
        return new BaseAttributeKvEntry(lastUpdateTs, kv);
    }
}
