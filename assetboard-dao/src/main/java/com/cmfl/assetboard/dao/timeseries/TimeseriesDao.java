package com.cmfl.assetboard.dao.timeseries;

import com.cmfl.assetboard.common.kv.TsKvEntry;

import java.util.List;
import java.util.UUID;

public interface TimeseriesDao {
    void save(UUID entityId, TsKvEntry entry);
    void saveBatch(UUID entityId, List<TsKvEntry> entries);
    List<TsKvEntry> find(UUID entityId, String key, long startTs, long endTs, int limit);
    TsKvEntry findLatest(UUID entityId, String key);
    List<TsKvEntry> findAllLatest(UUID entityId);
    void delete(UUID entityId, String key, long startTs, long endTs);
}
