package com.cmfl.assetboard.dao.sql.repository;

import com.cmfl.assetboard.dao.sql.entity.AssetProfileEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AssetProfileRepository extends JpaRepository<AssetProfileEntity, UUID> {
    Page<AssetProfileEntity> findByTenantId(UUID tenantId, Pageable pageable);
    Optional<AssetProfileEntity> findByTenantIdAndName(UUID tenantId, String name);
}
