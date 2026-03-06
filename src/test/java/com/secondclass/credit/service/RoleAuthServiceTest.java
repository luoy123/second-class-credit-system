package com.secondclass.credit.service;

import com.secondclass.credit.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleAuthServiceTest {

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private RoleAuthService roleAuthService;

    @Test
    void requireAdminShouldPassWhenRoleHeaderIsAdmin() {
        assertDoesNotThrow(() -> roleAuthService.requireAdmin("ADMIN"));
        assertDoesNotThrow(() -> roleAuthService.requireAdmin("admin"));
    }

    @Test
    void requireAdminShouldPassWhenAuthorizationTokenContainsAdminRole() {
        when(jwtService.extractRole("token")).thenReturn("ADMIN");
        assertDoesNotThrow(() -> roleAuthService.requireAdmin(null, "Bearer token"));
    }

    @Test
    void requireAdminShouldThrowWhenRoleMissing() {
        BusinessException ex = assertThrows(BusinessException.class, () -> roleAuthService.requireAdmin(null, null));
        assertEquals(4030, ex.getCode());
    }

    @Test
    void requireAdminShouldThrowWhenTokenFormatInvalid() {
        BusinessException ex = assertThrows(BusinessException.class, () -> roleAuthService.requireAdmin(null, "token"));
        assertEquals(4032, ex.getCode());
    }

    @Test
    void requireAdminShouldThrowWhenRoleNotAdmin() {
        when(jwtService.extractRole("token")).thenReturn("STUDENT");
        BusinessException ex = assertThrows(BusinessException.class, () -> roleAuthService.requireAdmin(null, "Bearer token"));
        assertEquals(4031, ex.getCode());
    }
}
