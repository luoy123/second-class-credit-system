package com.secondclass.credit.service;

import com.secondclass.credit.domain.dto.AuthLoginRequest;
import com.secondclass.credit.domain.dto.AuthTokenResponse;
import com.secondclass.credit.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final String ADMIN_ROLE = "ADMIN";

    private final JwtService jwtService;

    @Value("${auth.admin.username}")
    private String adminUsername;

    @Value("${auth.admin.password}")
    private String adminPassword;

    public AuthService(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public AuthTokenResponse login(AuthLoginRequest request) {
        if (!adminUsername.equals(request.getUsername()) || !adminPassword.equals(request.getPassword())) {
            throw new BusinessException(4010, "用户名或密码错误");
        }
        String token = jwtService.generateToken(request.getUsername(), ADMIN_ROLE);
        return AuthTokenResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpireSeconds())
                .role(ADMIN_ROLE)
                .build();
    }
}
