package com.secondclass.credit.service;

import com.secondclass.credit.domain.dto.CreditRuleCreateRequest;
import com.secondclass.credit.domain.entity.CreditRule;
import com.secondclass.credit.exception.BusinessException;
import com.secondclass.credit.repository.CreditRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CreditRuleService {

    private final CreditRuleRepository creditRuleRepository;

    public CreditRule create(CreditRuleCreateRequest request) {
        if (creditRuleRepository.existsByCategory(request.getCategory())) {
            throw new BusinessException("学分规则分类已存在：" + request.getCategory());
        }
        CreditRule creditRule = new CreditRule();
        creditRule.setCategory(request.getCategory());
        creditRule.setBaseCredit(request.getBaseCredit());
        creditRule.setDescription(request.getDescription());
        creditRule.setEnabled(request.getEnabled() == null || request.getEnabled());
        return creditRuleRepository.save(creditRule);
    }

    public List<CreditRule> list() {
        return creditRuleRepository.findAll();
    }
}
