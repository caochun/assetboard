package com.cmfl.assetboard.dao.sql.repository;

import com.cmfl.assetboard.dao.sql.entity.DataSourceConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DataSourceConfigRepository extends JpaRepository<DataSourceConfigEntity, UUID> {
    List<DataSourceConfigEntity> findByAssetId(UUID assetId);
    Optional<DataSourceConfigEntity> findByAssetIdAndCollectorId(UUID assetId, String collectorId);
}
