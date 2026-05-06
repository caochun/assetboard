package com.cmfl.assetboard.dao.sql.repository;

import com.cmfl.assetboard.dao.sql.entity.AlarmEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AlarmRepository extends JpaRepository<AlarmEntity, UUID> {
    Page<AlarmEntity> findByTenantId(UUID tenantId, Pageable pageable);
    Page<AlarmEntity> findByOriginatorIdAndOriginatorType(UUID originatorId, Integer originatorType, Pageable pageable);
    List<AlarmEntity> findByOriginatorIdAndTypeAndClearedFalse(UUID originatorId, String type);
}
