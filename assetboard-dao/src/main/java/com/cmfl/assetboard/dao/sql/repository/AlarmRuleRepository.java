package com.cmfl.assetboard.dao.sql.repository;

import com.cmfl.assetboard.dao.sql.entity.AlarmRuleEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AlarmRuleRepository extends JpaRepository<AlarmRuleEntity, UUID> {
    Page<AlarmRuleEntity> findByTenantId(UUID tenantId, Pageable pageable);
    List<AlarmRuleEntity> findByEnabledTrueAndTelemetryKey(String telemetryKey);
}
