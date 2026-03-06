package com.secondclass.credit.service;

import com.secondclass.credit.domain.dto.AuthLoginRequest;
import com.secondclass.credit.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void loginShouldReturnTokenWhenCredentialValid() {
        ReflectionTestUtils.setField(authService, "adminUsername", "admin");
        ReflectionTestUtils.setField(authService, "adminPassword", "admin123");

        AuthLoginRequest request = new AuthLoginRequest();
        request.setUsername("admin");
        request.setPassword("admin123");

        when(jwtService.generateToken("admin", "ADMIN")).thenReturn("mock-token");
        when(jwtService.getExpireSeconds()).thenReturn(7200L);

        var result = authService.login(request);

        assertEquals("mock-token", result.getToken());
        assertEquals("Bearer", result.getTokenType());
        assertEquals(7200L, result.getExpiresIn());
        assertEquals("ADMIN", result.getRole());
    }

    @Test
    void loginShouldThrowWhenCredentialInvalid() {
        ReflectionTestUtils.setField(authService, "adminUsername", "admin");
        ReflectionTestUtils.setField(authService, "adminPassword", "admin123");

        AuthLoginRequest request = new AuthLoginRequest();
        request.setUsername("admin");
        request.setPassword("wrong");

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.login(request));
        assertEquals(4010, ex.getCode());
    }
}
