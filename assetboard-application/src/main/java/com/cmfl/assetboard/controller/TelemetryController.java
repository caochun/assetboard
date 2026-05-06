package com.cmfl.assetboard.controller;

import com.cmfl.assetboard.common.kv.*;
import com.cmfl.assetboard.service.TelemetryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/plugins/telemetry")
public class TelemetryController {

    private final TelemetryService telemetryService;

    public TelemetryController(TelemetryService telemetryService) {
        this.telemetryService = telemetryService;
    }

    @PostMapping("/{entityId}/timeseries")
    public void saveTimeseries(
            @PathVariable UUID entityId,
            @RequestParam(defaultValue = "0") long ts,
            @RequestBody Map<String, Object> values) {
        long timestamp = ts > 0 ? ts : System.currentTimeMillis();
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            KvEntry kv = toKvEntry(entry.getKey(), entry.getValue());
            telemetryService.saveTimeseries(entityId, new BasicTsKvEntry(timestamp, kv));
        }
    }

    @GetMapping("/{entityId}/timeseries")
    public List<TsKvEntry> getTimeseries(
            @PathVariable UUID entityId,
            @RequestParam String key,
            @RequestParam long startTs,
            @RequestParam long endTs,
            @RequestParam(defaultValue = "1000") int limit) {
        return telemetryService.findTimeseries(entityId, key, startTs, endTs, limit);
    }

    @GetMapping("/{entityId}/timeseries/latest")
    public List<TsKvEntry> getLatestTimeseries(@PathVariable UUID entityId) {
        return telemetryService.findAllLatestTimeseries(entityId);
    }

    @PostMapping("/{entityId}/attributes")
    public void saveAttributes(
            @PathVariable UUID entityId,
            @RequestBody Map<String, Object> values) {
        long now = System.currentTimeMillis();
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            KvEntry kv = toKvEntry(entry.getKey(), entry.getValue());
            telemetryService.saveAttribute(entityId, new BaseAttributeKvEntry(now, kv));
        }
    }

    @GetMapping("/{entityId}/attributes")
    public List<AttributeKvEntry> getAttributes(@PathVariable UUID entityId) {
        return telemetryService.findAllAttributes(entityId);
    }

    private KvEntry toKvEntry(String key, Object value) {
        if (value instanceof Boolean b) return BasicKvEntry.ofBoolean(key, b);
        if (value instanceof Integer i) return BasicKvEntry.ofLong(key, i.longValue());
        if (value instanceof Long l) return BasicKvEntry.ofLong(key, l);
        if (value instanceof Double d) return BasicKvEntry.ofDouble(key, d);
        if (value instanceof Number n) return BasicKvEntry.ofDouble(key, n.doubleValue());
        return BasicKvEntry.ofString(key, String.valueOf(value));
    }
}
