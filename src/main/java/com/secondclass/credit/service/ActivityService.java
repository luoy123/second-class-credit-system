package com.secondclass.credit.service;

import com.secondclass.credit.domain.entity.Activity;
import com.secondclass.credit.exception.BusinessException;
import com.secondclass.credit.repository.ActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityRepository activityRepository;

    public Activity create(Activity activity) {
        return activityRepository.save(activity);
    }

    public Activity findById(Long id) {
        return activityRepository.findById(id)
                .orElseThrow(() -> new BusinessException("活动不存在，id=" + id));
    }

    public List<Activity> list() {
        return activityRepository.findAll();
    }
}
