package com.cmfl.assetboard.dao.sql.entity;

import com.cmfl.assetboard.common.data.Project;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
@Table(name = "project")
public class ProjectEntity {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Id
    private UUID id;
    private Long createdTime;
    private UUID tenantId;
    private UUID customerId;
    private String name;
    private String projectNo;
    private String businessType;
    private String leaseType;
    @Lob
    private String additionalInfo;

    public Project toData() {
        Project p = new Project();
        p.setId(id);
        p.setCreatedTime(createdTime != null ? createdTime : 0);
        p.setTenantId(tenantId);
        p.setCustomerId(customerId);
        p.setName(name);
        p.setProjectNo(projectNo);
        p.setBusinessType(businessType);
        p.setLeaseType(leaseType);
        try {
            if (additionalInfo != null) {
                p.setAdditionalInfo(MAPPER.readTree(additionalInfo));
            }
        } catch (Exception ignored) {
        }
        return p;
    }

    public static ProjectEntity fromData(Project data) {
        ProjectEntity e = new ProjectEntity();
        e.setId(data.getId());
        e.setCreatedTime(data.getCreatedTime());
        e.setTenantId(data.getTenantId());
        e.setCustomerId(data.getCustomerId());
        e.setName(data.getName());
        e.setProjectNo(data.getProjectNo());
        e.setBusinessType(data.getBusinessType());
        e.setLeaseType(data.getLeaseType());
        if (data.getAdditionalInfo() != null) {
            e.setAdditionalInfo(data.getAdditionalInfo().toString());
        }
        return e;
    }
}
