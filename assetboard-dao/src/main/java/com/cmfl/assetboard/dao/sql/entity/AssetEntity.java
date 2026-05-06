package com.cmfl.assetboard.dao.sql.entity;

import com.cmfl.assetboard.common.data.Asset;
import com.cmfl.assetboard.common.data.AssetStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
@Table(name = "asset")
public class AssetEntity {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Id
    private UUID id;
    private Long createdTime;
    private UUID tenantId;
    private UUID customerId;
    private UUID assetProfileId;
    private String name;
    private String type;
    private String label;
    private String status;
    @Lob
    private String additionalInfo;

    public Asset toData() {
        Asset asset = new Asset();
        asset.setId(id);
        asset.setCreatedTime(createdTime != null ? createdTime : 0);
        asset.setTenantId(tenantId);
        asset.setCustomerId(customerId);
        asset.setAssetProfileId(assetProfileId);
        asset.setName(name);
        asset.setType(type);
        asset.setLabel(label);
        asset.setStatus(status != null ? AssetStatus.valueOf(status) : AssetStatus.IN_LEASE);
        try {
            if (additionalInfo != null) {
                asset.setAdditionalInfo(MAPPER.readTree(additionalInfo));
            }
        } catch (Exception ignored) {
        }
        return asset;
    }

    public static AssetEntity fromData(Asset data) {
        AssetEntity entity = new AssetEntity();
        entity.setId(data.getId());
        entity.setCreatedTime(data.getCreatedTime());
        entity.setTenantId(data.getTenantId());
        entity.setCustomerId(data.getCustomerId());
        entity.setAssetProfileId(data.getAssetProfileId());
        entity.setName(data.getName());
        entity.setType(data.getType());
        entity.setLabel(data.getLabel());
        entity.setStatus(data.getStatus() != null ? data.getStatus().name() : "IN_LEASE");
        JsonNode info = data.getAdditionalInfo();
        if (info != null) {
            entity.setAdditionalInfo(info.toString());
        }
        return entity;
    }
}
