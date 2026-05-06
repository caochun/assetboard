package com.cmfl.assetboard.controller;

import com.cmfl.assetboard.common.data.Alarm;
import com.cmfl.assetboard.common.query.PageData;
import com.cmfl.assetboard.common.query.PageLink;
import com.cmfl.assetboard.service.AlarmService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/alarm")
public class AlarmController {

    private final AlarmService alarmService;

    public AlarmController(AlarmService alarmService) {
        this.alarmService = alarmService;
    }

    @PostMapping
    public Alarm create(@RequestBody Alarm alarm) {
        return alarmService.createOrUpdate(alarm);
    }

    @GetMapping("/{alarmId}")
    public ResponseEntity<Alarm> getById(@PathVariable UUID alarmId) {
        return alarmService.findById(alarmId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public PageData<Alarm> getByTenantId(
            @RequestParam UUID tenantId,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(defaultValue = "0") int page) {
        return alarmService.findByTenantId(tenantId, new PageLink(pageSize, page));
    }

    @GetMapping("/originator/{originatorId}")
    public PageData<Alarm> getByOriginator(
            @PathVariable UUID originatorId,
            @RequestParam(defaultValue = "0") int originatorType,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(defaultValue = "0") int page) {
        return alarmService.findByOriginator(originatorId, originatorType, new PageLink(pageSize, page));
    }

    @PostMapping("/{alarmId}/ack")
    public Alarm acknowledge(@PathVariable UUID alarmId) {
        return alarmService.acknowledge(alarmId);
    }

    @PostMapping("/{alarmId}/clear")
    public Alarm clear(@PathVariable UUID alarmId) {
        return alarmService.clear(alarmId);
    }
}
