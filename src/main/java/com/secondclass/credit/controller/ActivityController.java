package com.secondclass.credit.controller;

import com.secondclass.credit.common.ApiResponse;
import com.secondclass.credit.domain.dto.ActivityCreateRequest;
import com.secondclass.credit.domain.entity.Activity;
import com.secondclass.credit.service.ActivityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/activities")
public class ActivityController {

    private final ActivityService activityService;

    @PostMapping
    public ApiResponse<Activity> create(@Valid @RequestBody ActivityCreateRequest request) {
        Activity activity = new Activity();
        activity.setTitle(request.getTitle());
        activity.setCategory(request.getCategory());
        activity.setOrganizer(request.getOrganizer());
        activity.setActivityDate(request.getActivityDate());
        activity.setMaxCredit(request.getMaxCredit());
        return ApiResponse.success(activityService.create(activity));
    }

    @GetMapping("/{id}")
    public ApiResponse<Activity> findById(@PathVariable Long id) {
        return ApiResponse.success(activityService.findById(id));
    }

    @GetMapping
    public ApiResponse<List<Activity>> list() {
        return ApiResponse.success(activityService.list());
    }
}
