package com.secondclass.credit.service;

import com.secondclass.credit.exception.BusinessException;
import org.springframework.stereotype.Service;

@Service
public class RoleAuthService {

    private static final String ADMIN_ROLE = "ADMIN";

    public void requireAdmin(String roleHeader) {
        if (roleHeader == null || roleHeader.isBlank()) {
            throw new BusinessException(4030, "缺少角色信息，请在请求头提供 X-Role");
        }
        if (!ADMIN_ROLE.equalsIgnoreCase(roleHeader.trim())) {
            throw new BusinessException(4031, "当前接口仅管理员可访问");
        }
    }
}
