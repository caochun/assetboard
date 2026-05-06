package com.cmfl.assetboard.service;

import com.cmfl.assetboard.common.data.Asset;
import com.cmfl.assetboard.common.query.PageData;
import com.cmfl.assetboard.common.query.PageLink;
import com.cmfl.assetboard.dao.sql.entity.AssetEntity;
import com.cmfl.assetboard.dao.sql.repository.AssetRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class AssetService {

    private final AssetRepository assetRepository;

    public AssetService(AssetRepository assetRepository) {
        this.assetRepository = assetRepository;
    }

    @Transactional
    public Asset save(Asset asset) {
        if (asset.getId() == null) {
            asset.setId(UUID.randomUUID());
            asset.setCreatedTime(System.currentTimeMillis());
        }
        AssetEntity entity = AssetEntity.fromData(asset);
        return assetRepository.save(entity).toData();
    }

    public Optional<Asset> findById(UUID id) {
        return assetRepository.findById(id).map(AssetEntity::toData);
    }

    public PageData<Asset> findByTenantId(UUID tenantId, PageLink pageLink) {
        Page<AssetEntity> page = assetRepository.findByTenantId(tenantId, PageRequest.of(pageLink.getPage(), pageLink.getPageSize()));
        return toPageData(page);
    }

    public PageData<Asset> findByTenantIdAndType(UUID tenantId, String type, PageLink pageLink) {
        Page<AssetEntity> page;
        if (tenantId != null) {
            page = assetRepository.findByTenantIdAndType(tenantId, type, PageRequest.of(pageLink.getPage(), pageLink.getPageSize()));
        } else {
            page = assetRepository.findByType(type, PageRequest.of(pageLink.getPage(), pageLink.getPageSize()));
        }
        return toPageData(page);
    }

    public PageData<Asset> findByCustomerId(UUID tenantId, UUID customerId, PageLink pageLink) {
        Page<AssetEntity> page = assetRepository.findByTenantIdAndCustomerId(tenantId, customerId,
                PageRequest.of(pageLink.getPage(), pageLink.getPageSize()));
        return toPageData(page);
    }

    @Transactional
    public void deleteById(UUID id) {
        assetRepository.deleteById(id);
    }

    private PageData<Asset> toPageData(Page<AssetEntity> page) {
        return new PageData<>(
                page.getContent().stream().map(AssetEntity::toData).toList(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.hasNext());
    }
}
