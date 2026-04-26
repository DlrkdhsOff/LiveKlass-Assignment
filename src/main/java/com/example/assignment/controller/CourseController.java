package com.example.assignment.controller;

import com.example.assignment.domain.dto.ResultResponse;
import com.example.assignment.domain.dto.request.CourseReq;
import com.example.assignment.domain.dto.request.CourseSearchReq;
import com.example.assignment.domain.dto.request.CourseStatusReq;
import com.example.assignment.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "강의 API", description = "강의 등록, 조회, 상태 변경, 수강생 목록 조회")
@RestController
@RequestMapping("/api/v1/course")
@RequiredArgsConstructor
public class CourseController {

  private final CourseService courseService;

  /**
   * 강의 등록
   * POST /api/v1/course
   * - 강사 전용
   * - 동일한 강의 중복 등록 불가
   */
  @PostMapping
  @Operation(summary = "강의 등록", description = "강사 전용. 동일한 강의 중복 등록 불가.")
  public ResponseEntity<ResultResponse> register(@RequestBody @Valid CourseReq courseReq) {

    ResultResponse response = courseService.register(courseReq);
    return new ResponseEntity<>(response, response.getStatus());
  }

  /**
   * 강의 목록 조회
   * GET /api/v1/course
   * - 모든 조건은 선택값 (없으면 전체 조회)
   * - 강사명, 제목, 가격 범위, 기간, 상태로 필터링 가능
   * - 페이지네이션 적용 (기본 1페이지)
   */
  @GetMapping
  @Operation(summary = "강의 목록 조회", description = "모든 조건은 선택값. 강사명, 제목, 가격 범위, 기간, 상태로 필터링 가능.")
  public ResponseEntity<ResultResponse> getCourses(
      @ParameterObject @ModelAttribute CourseSearchReq searchReq,
      @RequestParam(defaultValue = "1") int page) {

    ResultResponse response = courseService.getCourses(searchReq, page);

    return new ResponseEntity<>(response, response.getStatus());
  }

  /**
   * 강의 상세 조회
   * GET /api/v1/course/{courseId}
   * - 현재 수강 신청 인원 포함
   */
  @GetMapping("/{courseId}")
  @Operation(summary = "강의 상세 조회", description = "현재 수강 신청 인원 포함.")
  public ResponseEntity<ResultResponse> getCourseDetail(@PathVariable Long courseId) {

    ResultResponse response = courseService.getCourseDetail(courseId);
    return new ResponseEntity<>(response, response.getStatus());
  }

  /**
   * 강의 상태 변경
   * PATCH /api/v1/course/{userId}/{courseId}/status
   * - 강사 본인 강의만 변경 가능
   * - 수강 기간이 만료된 강의는 변경 불가
   * - 허용 전환: DRAFT → OPEN, DRAFT → CLOSED, OPEN → CLOSED
   * - 불가 전환: CLOSED → OPEN, CLOSED → CLOSED, OPEN → OPEN
   */
  @PatchMapping("/{userId}/{courseId}/status")
  @Operation(summary = "강의 상태 변경", description = "강사 본인 강의만 변경 가능. 허용 전환: DRAFT → OPEN, DRAFT → CLOSED, OPEN → CLOSED")
  public ResponseEntity<ResultResponse> updateCourseStatus(
      @PathVariable Long userId,
      @PathVariable Long courseId,
      @RequestBody @Valid CourseStatusReq courseStatusReq) {

    ResultResponse response = courseService.updateCourseStatus(userId, courseId, courseStatusReq);
    return new ResponseEntity<>(response, response.getStatus());
  }

  /**
   * 강의별 수강생 목록 조회
   * GET /api/v1/course/{userId}/{courseId}/enrollments
   * - 강사 전용 (본인 강의만 조회 가능)
   * - 최신순 정렬, 페이지네이션 적용
   */
  @GetMapping("/{userId}/{courseId}/enrollments")
  @Operation(summary = "강의별 수강생 목록 조회", description = "강사 전용. 본인 강의만 조회 가능. 최신순 정렬.")
  public ResponseEntity<ResultResponse> getCourseEnrollments(
      @PathVariable Long userId,
      @PathVariable Long courseId,
      @RequestParam(defaultValue = "1") int page) {

    ResultResponse response = courseService.getCourseEnrollments(userId, courseId, page);
    return new ResponseEntity<>(response, response.getStatus());
  }
}
