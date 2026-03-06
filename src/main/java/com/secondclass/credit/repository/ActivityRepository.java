package com.secondclass.credit.repository;

import com.secondclass.credit.domain.entity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityRepository extends JpaRepository<Activity, Long> {
}
