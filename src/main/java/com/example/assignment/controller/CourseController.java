package com.example.assignment.controller;

import com.example.assignment.domain.dto.ResultResponse;
import com.example.assignment.domain.dto.request.CourseReq;
import com.example.assignment.domain.type.CourseStatus;
import com.example.assignment.service.CourseService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/course")
@RequiredArgsConstructor
public class CourseController {

  private final CourseService courseService;

  @PostMapping
  public ResponseEntity<ResultResponse> register(@RequestBody @Valid CourseReq courseReq) {

    ResultResponse response = courseService.register(courseReq);
    return new ResponseEntity<>(response, response.getStatus());
  }

  @GetMapping
  public ResponseEntity<ResultResponse> getCourses(
      @RequestParam(required = false) String creatorName,
      @RequestParam(required = false) String title,
      @RequestParam(required = false) Long minAmount,
      @RequestParam(required = false) Long maxAmount,
      @RequestParam(required = false) LocalDate startPeriodAt,
      @RequestParam(required = false) LocalDate endPeriodAt,
      @RequestParam(required = false) CourseStatus courseStatus,
      @RequestParam(defaultValue = "1") int page
  ) {
    ResultResponse response = courseService.getCourses(creatorName, title,
        minAmount, maxAmount, startPeriodAt, endPeriodAt, courseStatus, page);

    return new ResponseEntity<>(response, response.getStatus());
  }

  @GetMapping("/{courseId}")
  public ResponseEntity<ResultResponse> getCourseDetail(@PathVariable Long courseId) {

    ResultResponse response = courseService.getCourseDetail(courseId);
    return new ResponseEntity<>(response, response.getStatus());
  }

  @GetMapping("/{userId}/{courseId}")
  public ResponseEntity<ResultResponse> updateCourseStatus(@PathVariable Long userId, @PathVariable Long courseId) {

    ResultResponse response = courseService.updateCourseStatus(userId, courseId);
    return new ResponseEntity<>(response, response.getStatus());
  }

}
