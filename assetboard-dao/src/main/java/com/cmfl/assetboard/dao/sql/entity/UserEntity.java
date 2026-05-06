package com.cmfl.assetboard.dao.sql.entity;

import com.cmfl.assetboard.common.data.Authority;
import com.cmfl.assetboard.common.data.User;
import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
@Table(name = "tb_user")
public class UserEntity {
    @Id
    private UUID id;
    private Long createdTime;
    private UUID tenantId;
    @Column(unique = true)
    private String email;
    private String passwordHash;
    private String authority;
    private String name;

    public User toData() {
        User u = new User();
        u.setId(id);
        u.setCreatedTime(createdTime != null ? createdTime : 0);
        u.setTenantId(tenantId);
        u.setEmail(email);
        u.setPasswordHash(passwordHash);
        u.setAuthority(Authority.valueOf(authority));
        u.setName(name);
        return u;
    }

    public static UserEntity fromData(User data) {
        UserEntity e = new UserEntity();
        e.setId(data.getId());
        e.setCreatedTime(data.getCreatedTime());
        e.setTenantId(data.getTenantId());
        e.setEmail(data.getEmail());
        e.setPasswordHash(data.getPasswordHash());
        e.setAuthority(data.getAuthority().name());
        e.setName(data.getName());
        return e;
    }
}
