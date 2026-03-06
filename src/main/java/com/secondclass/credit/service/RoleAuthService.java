package com.secondclass.credit.service;

import com.secondclass.credit.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleAuthService {

    private static final String ADMIN_ROLE = "ADMIN";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    public void requireAdmin(String roleHeader) {
        requireAdmin(roleHeader, null);
    }

    public void requireAdmin(String roleHeader, String authorizationHeader) {
        String role = resolveRole(roleHeader, authorizationHeader);
        if (role == null || role.isBlank()) {
            throw new BusinessException(4030, "缺少角色信息，请在请求头提供 X-Role 或 Authorization");
        }
        if (!ADMIN_ROLE.equalsIgnoreCase(role.trim())) {
            throw new BusinessException(4031, "当前接口仅管理员可访问");
        }
    }

    private String resolveRole(String roleHeader, String authorizationHeader) {
        if (authorizationHeader != null && !authorizationHeader.isBlank()) {
            return resolveRoleFromAuthorization(authorizationHeader);
        }
        if (roleHeader == null || roleHeader.isBlank()) {
            return null;
        }
        return roleHeader.trim();
    }

    private String resolveRoleFromAuthorization(String authorizationHeader) {
        String value = authorizationHeader.trim();
        if (!value.startsWith(BEARER_PREFIX)) {
            throw new BusinessException(4032, "Authorization 格式错误，应为 Bearer <token>");
        }
        String token = value.substring(BEARER_PREFIX.length()).trim();
        if (token.isBlank()) {
            throw new BusinessException(4032, "Authorization 格式错误，应为 Bearer <token>");
        }
        try {
            return jwtService.extractRole(token);
        } catch (RuntimeException ex) {
            throw new BusinessException(4032, "无效或过期的访问令牌");
        }
    }
}
