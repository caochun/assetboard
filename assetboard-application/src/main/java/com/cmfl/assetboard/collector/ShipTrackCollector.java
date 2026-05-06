package com.cmfl.assetboard.collector;

import com.cmfl.assetboard.client.shipxy.ShipXyClient;
import com.cmfl.assetboard.common.data.Asset;
import com.cmfl.assetboard.common.kv.BasicKvEntry;
import com.cmfl.assetboard.common.kv.BasicTsKvEntry;
import com.cmfl.assetboard.common.kv.TsKvEntry;
import com.cmfl.assetboard.common.query.PageLink;
import com.cmfl.assetboard.service.AssetService;
import com.cmfl.assetboard.service.DataSourceConfigService;
import com.cmfl.assetboard.service.TelemetryService;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.ArrayList;
import java.util.List;

@Component
public class ShipTrackCollector implements DataCollector {

    private static final Logger log = LoggerFactory.getLogger(ShipTrackCollector.class);
    private static final String COLLECTOR_ID = "ais";
    private final ShipXyClient shipXyClient;
    private final AssetService assetService;
    private final TelemetryService telemetryService;
    private final DataSourceConfigService dataSourceConfigService;

    public ShipTrackCollector(ShipXyClient shipXyClient, AssetService assetService, TelemetryService telemetryService, DataSourceConfigService dataSourceConfigService) {
        this.shipXyClient = shipXyClient;
        this.assetService = assetService;
        this.telemetryService = telemetryService;
        this.dataSourceConfigService = dataSourceConfigService;
    }

    @Override
    public String getName() {
        return "ShipTrackCollector";
    }

    @Override
    @Scheduled(cron = "${collector.schedule.ship-track:-}")
    public void collect() {
        log.info("[{}] Starting collection", getName());
        long now = System.currentTimeMillis();
        long dayAgo = now - 86400_000L;
        var assets = assetService.findByTenantIdAndType(null, "vessel", new PageLink(1000, 0));
        for (Asset asset : assets.getData()) {
            if (!dataSourceConfigService.isCollectorEnabled(asset.getId(), COLLECTOR_ID)) continue;
            String imo = ShipArchiveCollector.getImo(asset);
            if (imo == null) continue;
            try {
                JsonNode resp = shipXyClient.getShipTrack(imo, dayAgo / 1000, now / 1000);
                JsonNode data = resp.path("data");
                if (!data.isArray()) continue;
                for (JsonNode point : data) {
                    long ts = point.path("utc").asLong() * 1000;
                    double lon = point.path("lon").asDouble() / 1_000_000.0;
                    double lat = point.path("lat").asDouble() / 1_000_000.0;
                    double sog = point.path("sog").asDouble() / 1000.0;
                    double cog = point.path("cog").asDouble() / 100.0;
                    List<TsKvEntry> entries = new ArrayList<>();
                    entries.add(new BasicTsKvEntry(ts, BasicKvEntry.ofDouble("lat", lat)));
                    entries.add(new BasicTsKvEntry(ts, BasicKvEntry.ofDouble("lon", lon)));
                    entries.add(new BasicTsKvEntry(ts, BasicKvEntry.ofDouble("sog", sog)));
                    entries.add(new BasicTsKvEntry(ts, BasicKvEntry.ofDouble("cog", cog)));
                    telemetryService.saveTimeseriesBatch(asset.getId(), entries);
                }
                log.info("[{}] Saved {} track points for {} (IMO: {})", getName(), data.size(), asset.getName(), imo);
            } catch (Exception e) {
                log.error("[{}] Failed for asset {} (IMO: {})", getName(), asset.getName(), imo, e);
            }
        }
        log.info("[{}] Collection complete", getName());
    }
}
