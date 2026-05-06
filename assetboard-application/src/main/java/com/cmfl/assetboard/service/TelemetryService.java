package com.cmfl.assetboard.service;

import com.cmfl.assetboard.common.kv.AttributeKvEntry;
import com.cmfl.assetboard.common.kv.TsKvEntry;
import com.cmfl.assetboard.dao.attributes.AttributesDao;
import com.cmfl.assetboard.dao.timeseries.TimeseriesDao;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TelemetryService {

    private final TimeseriesDao timeseriesDao;
    private final AttributesDao attributesDao;
    private final AlarmRuleEngine alarmRuleEngine;

    public TelemetryService(TimeseriesDao timeseriesDao, AttributesDao attributesDao, AlarmRuleEngine alarmRuleEngine) {
        this.timeseriesDao = timeseriesDao;
        this.attributesDao = attributesDao;
        this.alarmRuleEngine = alarmRuleEngine;
    }

    public void saveTimeseries(UUID entityId, TsKvEntry entry) {
        timeseriesDao.save(entityId, entry);
        alarmRuleEngine.evaluate(entityId, entry.getKey(), entry.getValue());
    }

    public void saveTimeseriesBatch(UUID entityId, List<TsKvEntry> entries) {
        timeseriesDao.saveBatch(entityId, entries);
        for (TsKvEntry entry : entries) {
            alarmRuleEngine.evaluate(entityId, entry.getKey(), entry.getValue());
        }
    }

    public List<TsKvEntry> findTimeseries(UUID entityId, String key, long startTs, long endTs, int limit) {
        return timeseriesDao.find(entityId, key, startTs, endTs, limit);
    }

    public TsKvEntry findLatestTimeseries(UUID entityId, String key) {
        return timeseriesDao.findLatest(entityId, key);
    }

    public List<TsKvEntry> findAllLatestTimeseries(UUID entityId) {
        return timeseriesDao.findAllLatest(entityId);
    }

    public void saveAttribute(UUID entityId, AttributeKvEntry entry) {
        attributesDao.save(entityId, entry);
    }

    public Optional<AttributeKvEntry> findAttribute(UUID entityId, String key) {
        return attributesDao.find(entityId, key);
    }

    public List<AttributeKvEntry> findAllAttributes(UUID entityId) {
        return attributesDao.findAll(entityId);
    }

    public void deleteAttribute(UUID entityId, String key) {
        attributesDao.delete(entityId, key);
    }
}
