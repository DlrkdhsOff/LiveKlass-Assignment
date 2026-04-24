package com.example.assignment.controller;

import com.example.assignment.domain.dto.ResultResponse;
import com.example.assignment.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/enrollment")
@RequiredArgsConstructor
public class EnrollmentController {

  private final EnrollmentService enrollmentService;

  @PostMapping("/{userId}/{courseId}")
  public ResponseEntity<ResultResponse> enroll(@PathVariable Long userId, @PathVariable Long courseId) {

    ResultResponse response = enrollmentService.enroll(userId, courseId);
    return new ResponseEntity<>(response, response.getStatus());
  }

  @PatchMapping("/{userId}/{enrollmentId}/pay")
  public ResponseEntity<ResultResponse> payEnroll(@PathVariable Long userId, @PathVariable Long enrollmentId) {

    ResultResponse response = enrollmentService.payEnroll(userId, enrollmentId);
    return new ResponseEntity<>(response, response.getStatus());
  }

  @GetMapping("/{userId}")
  public ResponseEntity<ResultResponse> getEnroll(@PathVariable Long userId, @RequestParam(defaultValue = "1") int page ) {

    ResultResponse response = enrollmentService.getEnroll(userId, page);
    return new ResponseEntity<>(response, response.getStatus());
  }
}