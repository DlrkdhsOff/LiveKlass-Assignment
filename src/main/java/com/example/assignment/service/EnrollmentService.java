package com.example.assignment.service;

import com.example.assignment.domain.dto.ResultResponse;

public interface EnrollmentService {

  ResultResponse enrollment(Long userId, Long courseId);

  ResultResponse payEnrollment(Long userId, Long enrollmentId);

  ResultResponse getEnrollments(Long userId, int page);

  ResultResponse cancelEnrollment(Long userId, Long enrollmentId);
}
