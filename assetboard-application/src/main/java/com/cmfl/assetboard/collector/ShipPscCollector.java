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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ShipPscCollector implements DataCollector {

    private static final Logger log = LoggerFactory.getLogger(ShipPscCollector.class);
    private static final String COLLECTOR_ID = "psc";
    private final ShipXyClient shipXyClient;
    private final AssetService assetService;
    private final AlarmService alarmService;
    private final DataSourceConfigService dataSourceConfigService;
    private final ObjectMapper mapper = new ObjectMapper();

    public ShipPscCollector(ShipXyClient shipXyClient, AssetService assetService, AlarmService alarmService, DataSourceConfigService dataSourceConfigService) {
        this.shipXyClient = shipXyClient;
        this.assetService = assetService;
        this.alarmService = alarmService;
        this.dataSourceConfigService = dataSourceConfigService;
    }

    @Override
    public String getName() {
        return "ShipPscCollector";
    }

    @Override
    @Scheduled(cron = "${collector.schedule.ship-psc:-}")
    public void collect() {
        log.info("[{}] Starting collection", getName());
        var assets = assetService.findByTenantIdAndType(null, "vessel", new PageLink(1000, 0));
        for (Asset asset : assets.getData()) {
            if (!dataSourceConfigService.isCollectorEnabled(asset.getId(), COLLECTOR_ID)) continue;
            String imo = ShipArchiveCollector.getImo(asset);
            if (imo == null) continue;
            try {
                JsonNode resp = shipXyClient.getPscHistory(imo);
                JsonNode data = resp.path("data");
                if (!data.isArray()) continue;
                for (JsonNode record : data) {
                    int defects = record.path("defectsNum").asInt(0);
                    if (defects <= 0) continue;
                    Alarm alarm = new Alarm();
                    alarm.setTenantId(asset.getTenantId());
                    alarm.setOriginatorId(asset.getId());
                    alarm.setOriginatorType(EntityType.ASSET);
                    alarm.setType("PSC_DEFICIENCY");
                    alarm.setSeverity(defects >= 3 ? AlarmSeverity.MAJOR : AlarmSeverity.MINOR);
                    alarm.setDetails(mapper.valueToTree(record));
                    alarmService.createOrUpdate(alarm);
                }
                log.info("[{}] Processed PSC records for {} (IMO: {})", getName(), asset.getName(), imo);
            } catch (Exception e) {
                log.error("[{}] Failed for asset {} (IMO: {})", getName(), asset.getName(), imo, e);
            }
        }
        log.info("[{}] Collection complete", getName());
    }
}
