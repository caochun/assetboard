package com.cmfl.assetboard.collector;

import com.cmfl.assetboard.client.clarksons.ClarksonsClient;
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

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Component
public class ValuationCollector implements DataCollector {

    private static final Logger log = LoggerFactory.getLogger(ValuationCollector.class);
    private static final String COLLECTOR_ID = "valuation";
    private final ClarksonsClient clarksonsClient;
    private final AssetService assetService;
    private final TelemetryService telemetryService;
    private final DataSourceConfigService dataSourceConfigService;

    public ValuationCollector(ClarksonsClient clarksonsClient, AssetService assetService, TelemetryService telemetryService, DataSourceConfigService dataSourceConfigService) {
        this.clarksonsClient = clarksonsClient;
        this.assetService = assetService;
        this.telemetryService = telemetryService;
        this.dataSourceConfigService = dataSourceConfigService;
    }

    @Override
    public String getName() {
        return "ValuationCollector";
    }

    @Override
    @Scheduled(cron = "${collector.schedule.valuation:-}")
    public void collect() {
        log.info("[{}] Starting collection", getName());
        var assets = assetService.findByTenantIdAndType(null, "vessel", new PageLink(1000, 0));
        for (Asset asset : assets.getData()) {
            if (!dataSourceConfigService.isCollectorEnabled(asset.getId(), COLLECTOR_ID)) continue;
            String imo = ShipArchiveCollector.getImo(asset);
            if (imo == null) continue;
            try {
                JsonNode resp = clarksonsClient.getAssetValueHistory(imo);
                JsonNode results = resp.path("results");
                if (!results.isArray()) continue;
                for (JsonNode record : results) {
                    long ts = parseDate(record.path("date").asText());
                    if (ts <= 0) continue;
                    double roughValue = record.path("roughValue").asDouble();
                    String currency = record.path("currency").asText("USD (m)");
                    List<TsKvEntry> entries = new ArrayList<>();
                    entries.add(new BasicTsKvEntry(ts, BasicKvEntry.ofDouble("roughValue", roughValue)));
                    entries.add(new BasicTsKvEntry(ts, BasicKvEntry.ofString("valuationCurrency", currency)));
                    telemetryService.saveTimeseriesBatch(asset.getId(), entries);
                }
                log.info("[{}] Saved {} valuation records for {} (IMO: {})", getName(), results.size(), asset.getName(), imo);
            } catch (Exception e) {
                log.error("[{}] Failed for asset {} (IMO: {})", getName(), asset.getName(), imo, e);
            }
        }
        log.info("[{}] Collection complete", getName());
    }

    private long parseDate(String dateStr) {
        try {
            return LocalDateTime.parse(dateStr).toInstant(ZoneOffset.UTC).toEpochMilli();
        } catch (DateTimeParseException e) {
            log.warn("Failed to parse date: {}", dateStr);
            return 0;
        }
    }
}
