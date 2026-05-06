package com.cmfl.assetboard.controller;

import com.cmfl.assetboard.common.data.AlarmRule;
import com.cmfl.assetboard.common.query.PageData;
import com.cmfl.assetboard.common.query.PageLink;
import com.cmfl.assetboard.service.AlarmRuleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/alarmRule")
public class AlarmRuleController {

    private final AlarmRuleService alarmRuleService;

    public AlarmRuleController(AlarmRuleService alarmRuleService) {
        this.alarmRuleService = alarmRuleService;
    }

    @PostMapping
    public AlarmRule save(@RequestBody AlarmRule rule) {
        return alarmRuleService.save(rule);
    }

    @GetMapping("/{ruleId}")
    public ResponseEntity<AlarmRule> getById(@PathVariable UUID ruleId) {
        return alarmRuleService.findById(ruleId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public PageData<AlarmRule> getByTenantId(
            @RequestParam UUID tenantId,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(defaultValue = "0") int page) {
        return alarmRuleService.findByTenantId(tenantId, new PageLink(pageSize, page));
    }

    @DeleteMapping("/{ruleId}")
    public void delete(@PathVariable UUID ruleId) {
        alarmRuleService.deleteById(ruleId);
    }
}
