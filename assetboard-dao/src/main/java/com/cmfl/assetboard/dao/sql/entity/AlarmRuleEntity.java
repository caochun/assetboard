package com.cmfl.assetboard.dao.sql.entity;

import com.cmfl.assetboard.common.data.AlarmRule;
import com.cmfl.assetboard.common.data.AlarmSeverity;
import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
@Table(name = "alarm_rule")
public class AlarmRuleEntity {
    @Id
    private UUID id;
    private Long createdTime;
    private UUID tenantId;
    private String name;
    private String targetType;
    private String telemetryKey;
    private String condition;
    private double threshold;
    private String severity;
    private String alarmType;
    private boolean enabled;

    public AlarmRule toData() {
        AlarmRule r = new AlarmRule();
        r.setId(id);
        r.setCreatedTime(createdTime != null ? createdTime : 0);
        r.setTenantId(tenantId);
        r.setName(name);
        r.setTargetType(targetType);
        r.setTelemetryKey(telemetryKey);
        r.setCondition(condition);
        r.setThreshold(threshold);
        r.setSeverity(AlarmSeverity.valueOf(severity));
        r.setAlarmType(alarmType);
        r.setEnabled(enabled);
        return r;
    }

    public static AlarmRuleEntity fromData(AlarmRule data) {
        AlarmRuleEntity e = new AlarmRuleEntity();
        e.setId(data.getId());
        e.setCreatedTime(data.getCreatedTime());
        e.setTenantId(data.getTenantId());
        e.setName(data.getName());
        e.setTargetType(data.getTargetType());
        e.setTelemetryKey(data.getTelemetryKey());
        e.setCondition(data.getCondition());
        e.setThreshold(data.getThreshold());
        e.setSeverity(data.getSeverity().name());
        e.setAlarmType(data.getAlarmType());
        e.setEnabled(data.isEnabled());
        return e;
    }
}
