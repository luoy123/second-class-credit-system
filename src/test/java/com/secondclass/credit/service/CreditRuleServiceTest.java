package com.secondclass.credit.service;

import com.secondclass.credit.domain.dto.CreditRuleCreateRequest;
import com.secondclass.credit.domain.entity.CreditRule;
import com.secondclass.credit.exception.BusinessException;
import com.secondclass.credit.repository.CreditRuleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreditRuleServiceTest {

    @Mock
    private CreditRuleRepository creditRuleRepository;

    @InjectMocks
    private CreditRuleService creditRuleService;

    @Test
    void createShouldDefaultEnabledToTrueWhenRequestValueIsNull() {
        CreditRuleCreateRequest request = new CreditRuleCreateRequest();
        request.setCategory("志愿服务");
        request.setBaseCredit(new BigDecimal("1.00"));
        request.setDescription("desc");
        request.setEnabled(null);

        when(creditRuleRepository.existsByCategory("志愿服务")).thenReturn(false);
        when(creditRuleRepository.save(any(CreditRule.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreditRule result = creditRuleService.create(request);

        assertEquals("志愿服务", result.getCategory());
        assertEquals(new BigDecimal("1.00"), result.getBaseCredit());
        assertEquals(Boolean.TRUE, result.getEnabled());
    }

    @Test
    void createShouldThrowWhenCategoryAlreadyExists() {
        CreditRuleCreateRequest request = new CreditRuleCreateRequest();
        request.setCategory("学术竞赛");
        request.setBaseCredit(new BigDecimal("2.00"));

        when(creditRuleRepository.existsByCategory("学术竞赛")).thenReturn(true);

        assertThrows(BusinessException.class, () -> creditRuleService.create(request));
    }
}
