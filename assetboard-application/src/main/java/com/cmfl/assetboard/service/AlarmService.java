package com.cmfl.assetboard.service;

import com.cmfl.assetboard.common.data.Alarm;
import com.cmfl.assetboard.common.query.PageData;
import com.cmfl.assetboard.common.query.PageLink;
import com.cmfl.assetboard.dao.sql.entity.AlarmEntity;
import com.cmfl.assetboard.dao.sql.repository.AlarmRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AlarmService {

    private final AlarmRepository alarmRepository;

    public AlarmService(AlarmRepository alarmRepository) {
        this.alarmRepository = alarmRepository;
    }

    @Transactional
    public Alarm createOrUpdate(Alarm alarm) {
        List<AlarmEntity> existing = alarmRepository
                .findByOriginatorIdAndTypeAndClearedFalse(alarm.getOriginatorId(), alarm.getType());
        if (!existing.isEmpty()) {
            AlarmEntity e = existing.get(0);
            e.setEndTs(System.currentTimeMillis());
            e.setSeverity(alarm.getSeverity().name());
            if (alarm.getDetails() != null) {
                e.setDetails(alarm.getDetails().toString());
            }
            return alarmRepository.save(e).toData();
        }
        if (alarm.getId() == null) {
            alarm.setId(UUID.randomUUID());
            alarm.setCreatedTime(System.currentTimeMillis());
        }
        if (alarm.getStartTs() == 0) {
            alarm.setStartTs(System.currentTimeMillis());
        }
        alarm.setEndTs(alarm.getStartTs());
        return alarmRepository.save(AlarmEntity.fromData(alarm)).toData();
    }

    @Transactional
    public Alarm acknowledge(UUID alarmId) {
        AlarmEntity e = alarmRepository.findById(alarmId).orElseThrow();
        e.setAcknowledged(true);
        e.setAckTs(System.currentTimeMillis());
        return alarmRepository.save(e).toData();
    }

    @Transactional
    public Alarm clear(UUID alarmId) {
        AlarmEntity e = alarmRepository.findById(alarmId).orElseThrow();
        e.setCleared(true);
        e.setClearTs(System.currentTimeMillis());
        return alarmRepository.save(e).toData();
    }

    public Optional<Alarm> findById(UUID id) {
        return alarmRepository.findById(id).map(AlarmEntity::toData);
    }

    public PageData<Alarm> findByTenantId(UUID tenantId, PageLink pageLink) {
        Page<AlarmEntity> page = alarmRepository.findByTenantId(tenantId,
                PageRequest.of(pageLink.getPage(), pageLink.getPageSize(), Sort.by(Sort.Direction.DESC, "createdTime")));
        return new PageData<>(
                page.getContent().stream().map(AlarmEntity::toData).toList(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.hasNext());
    }

    public PageData<Alarm> findByOriginator(UUID originatorId, int originatorType, PageLink pageLink) {
        Page<AlarmEntity> page = alarmRepository.findByOriginatorIdAndOriginatorType(originatorId, originatorType,
                PageRequest.of(pageLink.getPage(), pageLink.getPageSize(), Sort.by(Sort.Direction.DESC, "createdTime")));
        return new PageData<>(
                page.getContent().stream().map(AlarmEntity::toData).toList(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.hasNext());
    }
}
