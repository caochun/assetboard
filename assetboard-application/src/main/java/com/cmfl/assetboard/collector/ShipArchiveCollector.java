package com.cmfl.assetboard.collector;

import com.cmfl.assetboard.client.shipxy.ShipXyClient;
import com.cmfl.assetboard.common.data.Asset;
import com.cmfl.assetboard.common.kv.BaseAttributeKvEntry;
import com.cmfl.assetboard.common.kv.BasicKvEntry;
import com.cmfl.assetboard.common.query.PageLink;
import com.cmfl.assetboard.service.AssetService;
import com.cmfl.assetboard.service.DataSourceConfigService;
import com.cmfl.assetboard.service.TelemetryService;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Map;

@Component
public class ShipArchiveCollector implements DataCollector {

    private static final Logger log = LoggerFactory.getLogger(ShipArchiveCollector.class);
    private static final String COLLECTOR_ID = "archive";
    private final ShipXyClient shipXyClient;
    private final AssetService assetService;
    private final TelemetryService telemetryService;
    private final DataSourceConfigService dataSourceConfigService;

    public ShipArchiveCollector(ShipXyClient shipXyClient, AssetService assetService, TelemetryService telemetryService, DataSourceConfigService dataSourceConfigService) {
        this.shipXyClient = shipXyClient;
        this.assetService = assetService;
        this.telemetryService = telemetryService;
        this.dataSourceConfigService = dataSourceConfigService;
    }

    @Override
    public String getName() {
        return "ShipArchiveCollector";
    }

    @Override
    public void collect() {
        log.info("[{}] Starting collection", getName());
        var assets = assetService.findByTenantIdAndType(null, "vessel", new PageLink(1000, 0));
        for (Asset asset : assets.getData()) {
            if (!dataSourceConfigService.isCollectorEnabled(asset.getId(), COLLECTOR_ID)) continue;
            String imo = getImo(asset);
            if (imo == null) continue;
            try {
                JsonNode resp = shipXyClient.searchShipParticular(imo);
                JsonNode data = resp.path("data");
                if (data.isArray() && !data.isEmpty()) {
                    JsonNode ship = data.get(0);
                    long now = System.currentTimeMillis();
                    Iterator<Map.Entry<String, JsonNode>> fields = ship.fields();
                    while (fields.hasNext()) {
                        Map.Entry<String, JsonNode> field = fields.next();
                        String value = field.getValue().asText();
                        if (value != null && !value.isEmpty() && !"null".equals(value)) {
                            telemetryService.saveAttribute(asset.getId(),
                                    new BaseAttributeKvEntry(now, BasicKvEntry.ofString(field.getKey(), value)));
                        }
                    }
                    log.info("[{}] Updated attributes for {} (IMO: {})", getName(), asset.getName(), imo);
                }
            } catch (Exception e) {
                log.error("[{}] Failed for asset {} (IMO: {})", getName(), asset.getName(), imo, e);
            }
        }
        log.info("[{}] Collection complete", getName());
    }

    static String getImo(Asset asset) {
        if (asset.getAdditionalInfo() != null && asset.getAdditionalInfo().has("imo")) {
            return asset.getAdditionalInfo().get("imo").asText();
        }
        return null;
    }
}
