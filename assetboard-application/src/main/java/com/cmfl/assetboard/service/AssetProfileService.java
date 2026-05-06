package com.cmfl.assetboard.service;

import com.cmfl.assetboard.common.data.AssetProfile;
import com.cmfl.assetboard.common.query.PageData;
import com.cmfl.assetboard.common.query.PageLink;
import com.cmfl.assetboard.dao.sql.entity.AssetProfileEntity;
import com.cmfl.assetboard.dao.sql.repository.AssetProfileRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class AssetProfileService {

    private final AssetProfileRepository repository;

    public AssetProfileService(AssetProfileRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public AssetProfile save(AssetProfile profile) {
        if (profile.getId() == null) {
            profile.setId(UUID.randomUUID());
            profile.setCreatedTime(System.currentTimeMillis());
        }
        return repository.save(AssetProfileEntity.fromData(profile)).toData();
    }

    public Optional<AssetProfile> findById(UUID id) {
        return repository.findById(id).map(AssetProfileEntity::toData);
    }

    public PageData<AssetProfile> findByTenantId(UUID tenantId, PageLink pageLink) {
        Page<AssetProfileEntity> page = repository.findByTenantId(tenantId, PageRequest.of(pageLink.getPage(), pageLink.getPageSize()));
        return new PageData<>(
                page.getContent().stream().map(AssetProfileEntity::toData).toList(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.hasNext());
    }

    @Transactional
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }
}
