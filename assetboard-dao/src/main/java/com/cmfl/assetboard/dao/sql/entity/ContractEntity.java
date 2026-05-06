package com.cmfl.assetboard.dao.sql.entity;

import com.cmfl.assetboard.common.data.Contract;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Entity
@Table(name = "contract")
public class ContractEntity {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Id
    private UUID id;
    private Long createdTime;
    private UUID projectId;
    private String contractNo;
    private BigDecimal amount;
    private String currency;
    private String lessor;
    private String lessee;
    private String status;
    private Long signDate;
    @Lob
    private String additionalInfo;

    public Contract toData() {
        Contract c = new Contract();
        c.setId(id);
        c.setCreatedTime(createdTime != null ? createdTime : 0);
        c.setProjectId(projectId);
        c.setContractNo(contractNo);
        c.setAmount(amount);
        c.setCurrency(currency);
        c.setLessor(lessor);
        c.setLessee(lessee);
        c.setStatus(status);
        c.setSignDate(signDate != null ? signDate : 0);
        try {
            if (additionalInfo != null) {
                c.setAdditionalInfo(MAPPER.readTree(additionalInfo));
            }
        } catch (Exception ignored) {
        }
        return c;
    }

    public static ContractEntity fromData(Contract data) {
        ContractEntity e = new ContractEntity();
        e.setId(data.getId());
        e.setCreatedTime(data.getCreatedTime());
        e.setProjectId(data.getProjectId());
        e.setContractNo(data.getContractNo());
        e.setAmount(data.getAmount());
        e.setCurrency(data.getCurrency());
        e.setLessor(data.getLessor());
        e.setLessee(data.getLessee());
        e.setStatus(data.getStatus());
        e.setSignDate(data.getSignDate());
        if (data.getAdditionalInfo() != null) {
            e.setAdditionalInfo(data.getAdditionalInfo().toString());
        }
        return e;
    }
}
