package com.cmfl.assetboard.dao.sql.entity;

import com.cmfl.assetboard.common.data.EntityType;
import com.cmfl.assetboard.common.data.relation.EntityRelation;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

@Data
@Entity
@Table(name = "entity_relation")
@IdClass(EntityRelationEntity.RelationId.class)
public class EntityRelationEntity {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Id
    private UUID fromId;
    @Id
    private int fromType;
    @Id
    private UUID toId;
    @Id
    private int toType;
    @Id
    private String relationType;

    @Lob
    private String additionalInfo;

    @Data
    public static class RelationId implements Serializable {
        private UUID fromId;
        private int fromType;
        private UUID toId;
        private int toType;
        private String relationType;
    }

    public EntityRelation toData() {
        EntityRelation r = new EntityRelation();
        r.setFromId(fromId);
        r.setFromType(EntityType.values()[fromType]);
        r.setToId(toId);
        r.setToType(EntityType.values()[toType]);
        r.setRelationType(relationType);
        try {
            if (additionalInfo != null) {
                r.setAdditionalInfo(MAPPER.readTree(additionalInfo));
            }
        } catch (Exception ignored) {
        }
        return r;
    }

    public static EntityRelationEntity fromData(EntityRelation data) {
        EntityRelationEntity e = new EntityRelationEntity();
        e.setFromId(data.getFromId());
        e.setFromType(data.getFromType().ordinal());
        e.setToId(data.getToId());
        e.setToType(data.getToType().ordinal());
        e.setRelationType(data.getRelationType());
        if (data.getAdditionalInfo() != null) {
            e.setAdditionalInfo(data.getAdditionalInfo().toString());
        }
        return e;
    }
}
