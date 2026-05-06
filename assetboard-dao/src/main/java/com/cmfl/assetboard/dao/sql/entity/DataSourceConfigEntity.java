package com.cmfl.assetboard.dao.sql.entity;

import com.cmfl.assetboard.common.data.DataSourceConfig;
import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
@Table(name = "data_source_config")
public class DataSourceConfigEntity {
    @Id
    private UUID id;
    private Long createdTime;
    private UUID assetId;
    private String collectorId;
    private boolean enabled;
    @Lob
    private String params;

    public DataSourceConfig toData() {
        DataSourceConfig d = new DataSourceConfig();
        d.setId(id);
        d.setCreatedTime(createdTime != null ? createdTime : 0);
        d.setAssetId(assetId);
        d.setCollectorId(collectorId);
        d.setEnabled(enabled);
        d.setParams(params);
        return d;
    }

    public static DataSourceConfigEntity fromData(DataSourceConfig data) {
        DataSourceConfigEntity e = new DataSourceConfigEntity();
        e.setId(data.getId());
        e.setCreatedTime(data.getCreatedTime());
        e.setAssetId(data.getAssetId());
        e.setCollectorId(data.getCollectorId());
        e.setEnabled(data.isEnabled());
        e.setParams(data.getParams());
        return e;
    }
}
