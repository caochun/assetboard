package com.cmfl.assetboard.dao.sql.entity;

import com.cmfl.assetboard.common.data.Customer;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Entity
@Table(name = "customer")
public class CustomerEntity {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Id
    private UUID id;
    private Long createdTime;
    private UUID tenantId;
    private String name;
    private BigDecimal creditAmount;
    private BigDecimal remainingPrincipal;
    @Lob
    private String address;
    @Lob
    private String contactInfo;
    @Lob
    private String additionalInfo;

    public Customer toData() {
        Customer c = new Customer();
        c.setId(id);
        c.setCreatedTime(createdTime != null ? createdTime : 0);
        c.setTenantId(tenantId);
        c.setName(name);
        c.setCreditAmount(creditAmount);
        c.setRemainingPrincipal(remainingPrincipal);
        c.setAddress(address);
        c.setContactInfo(contactInfo);
        try {
            if (additionalInfo != null) {
                c.setAdditionalInfo(MAPPER.readTree(additionalInfo));
            }
        } catch (Exception ignored) {
        }
        return c;
    }

    public static CustomerEntity fromData(Customer data) {
        CustomerEntity e = new CustomerEntity();
        e.setId(data.getId());
        e.setCreatedTime(data.getCreatedTime());
        e.setTenantId(data.getTenantId());
        e.setName(data.getName());
        e.setCreditAmount(data.getCreditAmount());
        e.setRemainingPrincipal(data.getRemainingPrincipal());
        e.setAddress(data.getAddress());
        e.setContactInfo(data.getContactInfo());
        if (data.getAdditionalInfo() != null) {
            e.setAdditionalInfo(data.getAdditionalInfo().toString());
        }
        return e;
    }
}
