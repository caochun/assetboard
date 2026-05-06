package com.cmfl.assetboard.dao.sql.entity;

import com.cmfl.assetboard.common.data.AssetProfile;
import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
@Table(name = "asset_profile")
public class AssetProfileEntity {
    @Id
    private UUID id;
    private Long createdTime;
    private UUID tenantId;
    private String name;
    private String description;
    @Lob
    private String image;

    public AssetProfile toData() {
        AssetProfile profile = new AssetProfile();
        profile.setId(id);
        profile.setCreatedTime(createdTime != null ? createdTime : 0);
        profile.setTenantId(tenantId);
        profile.setName(name);
        profile.setDescription(description);
        profile.setImage(image);
        return profile;
    }

    public static AssetProfileEntity fromData(AssetProfile data) {
        AssetProfileEntity entity = new AssetProfileEntity();
        entity.setId(data.getId());
        entity.setCreatedTime(data.getCreatedTime());
        entity.setTenantId(data.getTenantId());
        entity.setName(data.getName());
        entity.setDescription(data.getDescription());
        entity.setImage(data.getImage());
        return entity;
    }
}
