package com.cmfl.assetboard.service;

import com.cmfl.assetboard.common.data.User;
import com.cmfl.assetboard.dao.sql.entity.UserEntity;
import com.cmfl.assetboard.dao.sql.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User createUser(User user, String rawPassword) {
        if (user.getId() == null) {
            user.setId(UUID.randomUUID());
            user.setCreatedTime(System.currentTimeMillis());
        }
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        return repository.save(UserEntity.fromData(user)).toData();
    }

    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email).map(UserEntity::toData);
    }

    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    public boolean checkPassword(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPasswordHash());
    }
}
