package com.cmfl.assetboard.dao.sql.repository;

import com.cmfl.assetboard.dao.sql.entity.EntityRelationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EntityRelationRepository extends JpaRepository<EntityRelationEntity, EntityRelationEntity.RelationId> {
    List<EntityRelationEntity> findByFromIdAndFromType(UUID fromId, int fromType);
    List<EntityRelationEntity> findByToIdAndToType(UUID toId, int toType);
}
