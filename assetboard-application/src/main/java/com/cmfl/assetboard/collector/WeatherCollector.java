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
public class WeatherCollector implements DataCollector {

    private static final Logger log = LoggerFactory.getLogger(WeatherCollector.class);
    private static final String COLLECTOR_ID = "weather";
    private final ShipXyClient shipXyClient;
    private final AssetService assetService;
    private final TelemetryService telemetryService;
    private final DataSourceConfigService dataSourceConfigService;

    public WeatherCollector(ShipXyClient shipXyClient, AssetService assetService, TelemetryService telemetryService, DataSourceConfigService dataSourceConfigService) {
        this.shipXyClient = shipXyClient;
        this.assetService = assetService;
        this.telemetryService = telemetryService;
        this.dataSourceConfigService = dataSourceConfigService;
    }

    @Override
    public String getName() {
        return "WeatherCollector";
    }

    @Override
    @Scheduled(cron = "${collector.schedule.weather:-}")
    public void collect() {
        log.info("[{}] Starting collection", getName());
        var assets = assetService.findByTenantIdAndType(null, "vessel", new PageLink(1000, 0));
        for (Asset asset : assets.getData()) {
            if (!dataSourceConfigService.isCollectorEnabled(asset.getId(), COLLECTOR_ID)) continue;
            try {
                TsKvEntry latEntry = telemetryService.findLatestTimeseries(asset.getId(), "lat");
                TsKvEntry lonEntry = telemetryService.findLatestTimeseries(asset.getId(), "lon");
                if (latEntry == null || lonEntry == null) continue;
                double lat = ((Number) latEntry.getValue()).doubleValue();
                double lon = ((Number) lonEntry.getValue()).doubleValue();
                JsonNode resp = shipXyClient.getWeatherByPoint(lon, lat);
                JsonNode data = resp.path("data");
                if (data.isMissingNode()) continue;
                long ts = System.currentTimeMillis();
                List<TsKvEntry> entries = new ArrayList<>();
                addDouble(entries, ts, data, "temperature");
                addDouble(entries, ts, data, "humidity");
                addDouble(entries, ts, data, "pressure");
                addDouble(entries, ts, data, "winddir");
                addDouble(entries, ts, data, "windspeed");
                addDouble(entries, ts, data, "visibility");
                addDouble(entries, ts, data, "waveheight");
                addDouble(entries, ts, data, "swellheight");
                telemetryService.saveTimeseriesBatch(asset.getId(), entries);
                log.info("[{}] Saved weather data for {}", getName(), asset.getName());
            } catch (Exception e) {
                log.error("[{}] Failed for asset {}", getName(), asset.getName(), e);
            }
        }
        log.info("[{}] Collection complete", getName());
    }

    private void addDouble(List<TsKvEntry> entries, long ts, JsonNode data, String key) {
        if (data.has(key)) {
            entries.add(new BasicTsKvEntry(ts, BasicKvEntry.ofDouble(key, data.path(key).asDouble())));
        }
    }
}
