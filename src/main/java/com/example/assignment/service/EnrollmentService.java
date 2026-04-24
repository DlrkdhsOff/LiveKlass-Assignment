package com.example.assignment.service;

import com.example.assignment.domain.dto.ResultResponse;

public interface EnrollmentService {

  ResultResponse enroll(Long userId, Long courseId);

  ResultResponse payEnroll(Long userId, Long enrollmentId);

  ResultResponse getEnroll(Long userId, int page);

  ResultResponse cancelEnroll(Long userId, Long enrollmentId);
}
