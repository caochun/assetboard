package com.cmfl.assetboard.dao.sql.repository;

import com.cmfl.assetboard.dao.sql.entity.ProjectEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProjectRepository extends JpaRepository<ProjectEntity, UUID> {
    Page<ProjectEntity> findByTenantId(UUID tenantId, Pageable pageable);
    Page<ProjectEntity> findByCustomerId(UUID customerId, Pageable pageable);
}
