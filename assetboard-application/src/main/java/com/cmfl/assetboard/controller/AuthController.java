package com.cmfl.assetboard.controller;

import com.cmfl.assetboard.common.data.User;
import com.cmfl.assetboard.security.JwtTokenProvider;
import com.cmfl.assetboard.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtTokenProvider tokenProvider;

    public AuthController(UserService userService, JwtTokenProvider tokenProvider) {
        this.userService = userService;
        this.tokenProvider = tokenProvider;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");
        User user = userService.findByEmail(email).orElse(null);
        if (user == null || !userService.checkPassword(user, password)) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }
        String token = tokenProvider.generateToken(user.getId(), user.getEmail(), user.getAuthority().name());
        return ResponseEntity.ok(Map.of(
                "token", token,
                "userId", user.getId(),
                "email", user.getEmail(),
                "name", user.getName(),
                "authority", user.getAuthority().name()
        ));
    }

    @GetMapping("/user")
    public ResponseEntity<?> currentUser(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).build();
        }
        var claims = tokenProvider.parseToken(authHeader.substring(7));
        String email = claims.get("email", String.class);
        User user = userService.findByEmail(email).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(Map.of(
                "userId", user.getId(),
                "email", user.getEmail(),
                "name", user.getName(),
                "authority", user.getAuthority().name(),
                "tenantId", user.getTenantId()
        ));
    }
}
