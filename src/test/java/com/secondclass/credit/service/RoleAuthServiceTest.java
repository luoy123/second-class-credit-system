package com.secondclass.credit.service;

import com.secondclass.credit.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RoleAuthServiceTest {

    private final RoleAuthService roleAuthService = new RoleAuthService();

    @Test
    void requireAdminShouldPassWhenRoleIsAdmin() {
        assertDoesNotThrow(() -> roleAuthService.requireAdmin("ADMIN"));
        assertDoesNotThrow(() -> roleAuthService.requireAdmin("admin"));
    }

    @Test
    void requireAdminShouldThrowWhenRoleMissing() {
        assertThrows(BusinessException.class, () -> roleAuthService.requireAdmin(null));
        assertThrows(BusinessException.class, () -> roleAuthService.requireAdmin(" "));
    }

    @Test
    void requireAdminShouldThrowWhenRoleNotAdmin() {
        assertThrows(BusinessException.class, () -> roleAuthService.requireAdmin("STUDENT"));
    }
}
