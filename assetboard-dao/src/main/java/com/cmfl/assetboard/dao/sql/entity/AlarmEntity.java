package com.cmfl.assetboard.dao.sql.entity;

import com.cmfl.assetboard.common.data.Alarm;
import com.cmfl.assetboard.common.data.AlarmSeverity;
import com.cmfl.assetboard.common.data.EntityType;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
@Table(name = "alarm")
public class AlarmEntity {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Id
    private UUID id;
    private Long createdTime;
    private UUID tenantId;
    private UUID originatorId;
    private Integer originatorType;
    private String type;
    private String severity;
    private Boolean acknowledged;
    private Boolean cleared;
    private Long startTs;
    private Long endTs;
    private Long ackTs;
    private Long clearTs;
    @Lob
    private String details;

    public Alarm toData() {
        Alarm a = new Alarm();
        a.setId(id);
        a.setCreatedTime(createdTime != null ? createdTime : 0);
        a.setTenantId(tenantId);
        a.setOriginatorId(originatorId);
        a.setOriginatorType(originatorType != null ? EntityType.values()[originatorType] : null);
        a.setType(type);
        a.setSeverity(severity != null ? AlarmSeverity.valueOf(severity) : null);
        a.setAcknowledged(Boolean.TRUE.equals(acknowledged));
        a.setCleared(Boolean.TRUE.equals(cleared));
        a.setStartTs(startTs != null ? startTs : 0);
        a.setEndTs(endTs != null ? endTs : 0);
        a.setAckTs(ackTs);
        a.setClearTs(clearTs);
        try {
            if (details != null) {
                a.setDetails(MAPPER.readTree(details));
            }
        } catch (Exception ignored) {
        }
        return a;
    }

    public static AlarmEntity fromData(Alarm data) {
        AlarmEntity e = new AlarmEntity();
        e.setId(data.getId());
        e.setCreatedTime(data.getCreatedTime());
        e.setTenantId(data.getTenantId());
        e.setOriginatorId(data.getOriginatorId());
        e.setOriginatorType(data.getOriginatorType() != null ? data.getOriginatorType().ordinal() : null);
        e.setType(data.getType());
        e.setSeverity(data.getSeverity() != null ? data.getSeverity().name() : null);
        e.setAcknowledged(data.isAcknowledged());
        e.setCleared(data.isCleared());
        e.setStartTs(data.getStartTs());
        e.setEndTs(data.getEndTs());
        e.setAckTs(data.getAckTs());
        e.setClearTs(data.getClearTs());
        if (data.getDetails() != null) {
            e.setDetails(data.getDetails().toString());
        }
        return e;
    }
}
