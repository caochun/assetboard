package com.cmfl.assetboard.collector;

import com.cmfl.assetboard.client.shipxy.ShipXyClient;
import com.cmfl.assetboard.common.data.Alarm;
import com.cmfl.assetboard.common.data.AlarmSeverity;
import com.cmfl.assetboard.common.data.EntityType;
import com.cmfl.assetboard.service.AlarmService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class NavWarningCollector implements DataCollector {

    private static final Logger log = LoggerFactory.getLogger(NavWarningCollector.class);
    private final ShipXyClient shipXyClient;
    private final AlarmService alarmService;
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${assetboard.tenant-id:00000000-0000-0000-0000-000000000001}")
    private UUID defaultTenantId;

    public NavWarningCollector(ShipXyClient shipXyClient, AlarmService alarmService) {
        this.shipXyClient = shipXyClient;
        this.alarmService = alarmService;
    }

    @Override
    public String getName() {
        return "NavWarningCollector";
    }

    @Override
    @Scheduled(cron = "${collector.schedule.nav-warning:-}")
    public void collect() {
        log.info("[{}] Starting collection", getName());
        try {
            JsonNode resp = shipXyClient.getNavWarningList();
            JsonNode data = resp.path("data");
            if (!data.isArray()) return;
            for (JsonNode warning : data) {
                Alarm alarm = new Alarm();
                alarm.setTenantId(defaultTenantId);
                alarm.setOriginatorId(defaultTenantId);
                alarm.setOriginatorType(EntityType.TENANT);
                alarm.setType("NAV_WARNING");
                String type = warning.path("type").asText("");
                alarm.setSeverity("军事演习".equals(type) ? AlarmSeverity.CRITICAL : AlarmSeverity.WARNING);
                alarm.setDetails(mapper.valueToTree(warning));
                alarmService.createOrUpdate(alarm);
            }
            log.info("[{}] Processed {} navigation warnings", getName(), data.size());
        } catch (Exception e) {
            log.error("[{}] Failed", getName(), e);
        }
    }
}
