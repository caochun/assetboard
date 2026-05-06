package com.cmfl.assetboard.dao.sql.repository;

import com.cmfl.assetboard.dao.sql.entity.AssetEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AssetRepository extends JpaRepository<AssetEntity, UUID> {
    Page<AssetEntity> findByTenantId(UUID tenantId, Pageable pageable);
    Page<AssetEntity> findByTenantIdAndType(UUID tenantId, String type, Pageable pageable);
    List<AssetEntity> findByTenantIdAndType(UUID tenantId, String type);
    Page<AssetEntity> findByType(String type, Pageable pageable);
    Page<AssetEntity> findByTenantIdAndCustomerId(UUID tenantId, UUID customerId, Pageable pageable);
}
