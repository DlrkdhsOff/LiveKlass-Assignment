package com.example.assignment.service;

import com.example.assignment.domain.dto.ResultResponse;
import com.example.assignment.domain.dto.request.CourseReq;
import com.example.assignment.domain.type.CourseStatus;
import jakarta.validation.Valid;
import java.time.LocalDate;

public interface CourseService {

  ResultResponse register(@Valid CourseReq courseReq);

  ResultResponse getCourses(String creatorName, String title, Long minAmount, Long maxAmount,
      LocalDate startPeriodAt, LocalDate endPeriodAt, CourseStatus courseStatus, int page);

  ResultResponse getCourseDetail(Long courseId);

  ResultResponse updateCourseStatus(Long userId, Long courseId);
}
