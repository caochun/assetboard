package com.cmfl.assetboard.collector;

import com.cmfl.assetboard.client.shipxy.ShipXyClient;
import com.cmfl.assetboard.common.data.Alarm;
import com.cmfl.assetboard.common.data.AlarmSeverity;
import com.cmfl.assetboard.common.data.Asset;
import com.cmfl.assetboard.common.data.EntityType;
import com.cmfl.assetboard.common.query.PageLink;
import com.cmfl.assetboard.service.AlarmService;
import com.cmfl.assetboard.service.AssetService;
import com.cmfl.assetboard.service.DataSourceConfigService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.HashMap;
import java.util.Map;

@Component
public class ShipAlertCollector implements DataCollector {

    private static final Logger log = LoggerFactory.getLogger(ShipAlertCollector.class);
    private static final String COLLECTOR_ID = "alert";
    private final ShipXyClient shipXyClient;
    private final AssetService assetService;
    private final AlarmService alarmService;
    private final DataSourceConfigService dataSourceConfigService;
    private final ObjectMapper mapper = new ObjectMapper();

    public ShipAlertCollector(ShipXyClient shipXyClient, AssetService assetService, AlarmService alarmService, DataSourceConfigService dataSourceConfigService) {
        this.shipXyClient = shipXyClient;
        this.assetService = assetService;
        this.alarmService = alarmService;
        this.dataSourceConfigService = dataSourceConfigService;
    }

    @Override
    public String getName() {
        return "ShipAlertCollector";
    }

    @Override
    @Scheduled(cron = "${collector.schedule.ship-alert:-}")
    public void collect() {
        log.info("[{}] Starting collection", getName());
        try {
            var assets = assetService.findByTenantIdAndType(null, "vessel", new PageLink(1000, 0));
            Map<String, Asset> imoToAsset = new HashMap<>();
            for (Asset asset : assets.getData()) {
                String imo = ShipArchiveCollector.getImo(asset);
                if (imo != null) imoToAsset.put(imo, asset);
            }

            JsonNode resp = shipXyClient.getShipAlertList();
            JsonNode data = resp.path("data");
            if (!data.isArray()) return;
            for (JsonNode alert : data) {
                String imo = alert.path("imo").asText();
                Asset asset = imoToAsset.get(imo);
                if (asset == null) continue;
                if (!dataSourceConfigService.isCollectorEnabled(asset.getId(), COLLECTOR_ID)) continue;
                String alertLevel = alert.path("alertLevel").asText("MEDIUM");
                Alarm alarm = new Alarm();
                alarm.setTenantId(asset.getTenantId());
                alarm.setOriginatorId(asset.getId());
                alarm.setOriginatorType(EntityType.ASSET);
                alarm.setType("SHIP_ALERT_" + alert.path("alertType").asText("UNKNOWN"));
                alarm.setSeverity("HIGH".equals(alertLevel) ? AlarmSeverity.CRITICAL : AlarmSeverity.WARNING);
                alarm.setDetails(mapper.valueToTree(alert));
                alarmService.createOrUpdate(alarm);
            }
            log.info("[{}] Processed {} alerts", getName(), data.size());
        } catch (Exception e) {
            log.error("[{}] Failed", getName(), e);
        }
    }
}
