package com.cmfl.assetboard.dao.attributes;

import com.cmfl.assetboard.common.kv.AttributeKvEntry;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AttributesDao {
    void save(UUID entityId, AttributeKvEntry entry);
    Optional<AttributeKvEntry> find(UUID entityId, String key);
    List<AttributeKvEntry> findAll(UUID entityId);
    void delete(UUID entityId, String key);
}
