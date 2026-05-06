package com.cmfl.assetboard.service;

import com.cmfl.assetboard.common.data.EntityType;
import com.cmfl.assetboard.common.data.relation.EntityRelation;
import com.cmfl.assetboard.dao.sql.entity.EntityRelationEntity;
import com.cmfl.assetboard.dao.sql.repository.EntityRelationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class RelationService {

    private final EntityRelationRepository relationRepository;

    public RelationService(EntityRelationRepository relationRepository) {
        this.relationRepository = relationRepository;
    }

    @Transactional
    public EntityRelation save(EntityRelation relation) {
        return relationRepository.save(EntityRelationEntity.fromData(relation)).toData();
    }

    public List<EntityRelation> findByFrom(UUID fromId, EntityType fromType) {
        return relationRepository.findByFromIdAndFromType(fromId, fromType.ordinal())
                .stream().map(EntityRelationEntity::toData).toList();
    }

    public List<EntityRelation> findByTo(UUID toId, EntityType toType) {
        return relationRepository.findByToIdAndToType(toId, toType.ordinal())
                .stream().map(EntityRelationEntity::toData).toList();
    }

    @Transactional
    public void delete(EntityRelation relation) {
        EntityRelationEntity.RelationId id = new EntityRelationEntity.RelationId();
        id.setFromId(relation.getFromId());
        id.setFromType(relation.getFromType().ordinal());
        id.setToId(relation.getToId());
        id.setToType(relation.getToType().ordinal());
        id.setRelationType(relation.getRelationType());
        relationRepository.deleteById(id);
    }
}
