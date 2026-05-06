package com.cmfl.assetboard.service;

import com.cmfl.assetboard.common.data.AlarmRule;
import com.cmfl.assetboard.common.query.PageData;
import com.cmfl.assetboard.common.query.PageLink;
import com.cmfl.assetboard.dao.sql.entity.AlarmRuleEntity;
import com.cmfl.assetboard.dao.sql.repository.AlarmRuleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AlarmRuleService {

    private final AlarmRuleRepository repository;

    public AlarmRuleService(AlarmRuleRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public AlarmRule save(AlarmRule rule) {
        if (rule.getId() == null) {
            rule.setId(UUID.randomUUID());
            rule.setCreatedTime(System.currentTimeMillis());
        }
        return repository.save(AlarmRuleEntity.fromData(rule)).toData();
    }

    public Optional<AlarmRule> findById(UUID id) {
        return repository.findById(id).map(AlarmRuleEntity::toData);
    }

    public PageData<AlarmRule> findByTenantId(UUID tenantId, PageLink pageLink) {
        Page<AlarmRuleEntity> page = repository.findByTenantId(tenantId, PageRequest.of(pageLink.getPage(), pageLink.getPageSize()));
        return new PageData<>(
                page.getContent().stream().map(AlarmRuleEntity::toData).toList(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.hasNext());
    }

    public List<AlarmRule> findEnabledByKey(String telemetryKey) {
        return repository.findByEnabledTrueAndTelemetryKey(telemetryKey)
                .stream().map(AlarmRuleEntity::toData).toList();
    }

    @Transactional
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }
}
