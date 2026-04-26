package com.example.assignment.service;

import com.example.assignment.domain.dto.ResultResponse;
import com.example.assignment.domain.dto.request.CourseReq;
import com.example.assignment.domain.dto.request.CourseSearchReq;
import com.example.assignment.domain.dto.request.CourseStatusReq;
import jakarta.validation.Valid;

public interface CourseService {

  ResultResponse register(@Valid CourseReq courseReq);

  ResultResponse getCourses(CourseSearchReq searchReq, int page);

  ResultResponse getCourseDetail(Long courseId);

  ResultResponse updateCourseStatus(Long userId, Long courseId, CourseStatusReq courseStatusReq);

  ResultResponse getCourseEnrollments(Long userId, Long courseId, int page);
}
