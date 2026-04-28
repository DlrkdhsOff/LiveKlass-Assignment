package com.example.assignment.creator.service;

import com.example.assignment.common.dto.ResultResponse;
import com.example.assignment.creator.dto.request.CourseReq;
import com.example.assignment.creator.dto.request.CourseStatusReq;
import jakarta.validation.Valid;

public interface CreatorService {

  ResultResponse register(@Valid CourseReq courseReq);

  ResultResponse updateCourseStatus(Long userId, Long courseId, CourseStatusReq courseStatusReq);

  ResultResponse getCourseEnrollments(Long userId, int page);

  ResultResponse getSaleRecord(Long userId, String yearMonth);
}
