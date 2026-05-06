package com.cmfl.assetboard.service;

import com.cmfl.assetboard.common.data.DataSourceConfig;
import com.cmfl.assetboard.dao.sql.entity.DataSourceConfigEntity;
import com.cmfl.assetboard.dao.sql.repository.DataSourceConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class DataSourceConfigService {

    private final DataSourceConfigRepository repository;

    public DataSourceConfigService(DataSourceConfigRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public DataSourceConfig save(DataSourceConfig config) {
        if (config.getId() == null) {
            config.setId(UUID.randomUUID());
            config.setCreatedTime(System.currentTimeMillis());
        }
        return repository.save(DataSourceConfigEntity.fromData(config)).toData();
    }

    public List<DataSourceConfig> findByAssetId(UUID assetId) {
        return repository.findByAssetId(assetId).stream()
                .map(DataSourceConfigEntity::toData).toList();
    }

    public boolean isCollectorEnabled(UUID assetId, String collectorId) {
        return repository.findByAssetIdAndCollectorId(assetId, collectorId)
                .map(DataSourceConfigEntity::isEnabled)
                .orElse(true);
    }

    @Transactional
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }
}
