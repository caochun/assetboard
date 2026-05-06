package com.cmfl.assetboard.service;

import com.cmfl.assetboard.common.data.Alarm;
import com.cmfl.assetboard.common.data.AlarmRule;
import com.cmfl.assetboard.common.data.Asset;
import com.cmfl.assetboard.common.data.EntityType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AlarmRuleEngine {

    private static final Logger log = LoggerFactory.getLogger(AlarmRuleEngine.class);
    private final AlarmRuleService alarmRuleService;
    private final AlarmService alarmService;
    private final AssetService assetService;
    private final ObjectMapper mapper = new ObjectMapper();

    public AlarmRuleEngine(AlarmRuleService alarmRuleService, AlarmService alarmService, AssetService assetService) {
        this.alarmRuleService = alarmRuleService;
        this.alarmService = alarmService;
        this.assetService = assetService;
    }

    public void evaluate(UUID entityId, String key, Object value) {
        if (!(value instanceof Number)) return;
        double numValue = ((Number) value).doubleValue();

        List<AlarmRule> rules = alarmRuleService.findEnabledByKey(key);
        if (rules.isEmpty()) return;

        Asset asset = assetService.findById(entityId).orElse(null);
        if (asset == null) return;

        for (AlarmRule rule : rules) {
            if (rule.getTargetType() != null && !rule.getTargetType().equals(asset.getType())) continue;
            if (matchCondition(rule.getCondition(), numValue, rule.getThreshold())) {
                Alarm alarm = new Alarm();
                alarm.setTenantId(asset.getTenantId());
                alarm.setOriginatorId(entityId);
                alarm.setOriginatorType(EntityType.ASSET);
                alarm.setType(rule.getAlarmType());
                alarm.setSeverity(rule.getSeverity());
                ObjectNode details = mapper.createObjectNode();
                details.put("ruleName", rule.getName());
                details.put("key", key);
                details.put("value", numValue);
                details.put("condition", rule.getCondition());
                details.put("threshold", rule.getThreshold());
                alarm.setDetails(details);
                alarmService.createOrUpdate(alarm);
                log.info("Rule '{}' triggered for entity {} ({}={}, {} {})",
                        rule.getName(), entityId, key, numValue, rule.getCondition(), rule.getThreshold());
            }
        }
    }

    private boolean matchCondition(String condition, double value, double threshold) {
        return switch (condition) {
            case "GT" -> value > threshold;
            case "GTE" -> value >= threshold;
            case "LT" -> value < threshold;
            case "LTE" -> value <= threshold;
            case "EQ" -> Math.abs(value - threshold) < 0.0001;
            default -> false;
        };
    }
}
