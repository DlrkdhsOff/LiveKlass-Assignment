package com.example.assignment.controller;

import com.example.assignment.domain.dto.ResultResponse;
import com.example.assignment.service.EnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "수강신청 API", description = "수강 신청, 결제, 조회, 취소")
@RestController
@RequestMapping("/api/v1/enrollment")
@RequiredArgsConstructor
public class EnrollmentController {

  private final EnrollmentService enrollmentService;

  /**
   * 수강 신청
   * POST /api/v1/enrollment/{userId}/{courseId}
   * - 정원 여유 있으면 PENDING 으로 등록
   * - 정원 초과 시 WAITLISTED 로 대기 등록
   */
  @PostMapping("/{userId}/{courseId}")
  @Operation(summary = "수강 신청", description = "정원 여유 시 PENDING, 정원 초과 시 WAITLISTED 로 등록.")
  public ResponseEntity<ResultResponse> enroll(
      @PathVariable Long userId,
      @PathVariable Long courseId) {

    ResultResponse response = enrollmentService.enrollment(userId, courseId);
    return new ResponseEntity<>(response, response.getStatus());
  }

  /**
   * 결제 확정
   * PATCH /api/v1/enrollment/{userId}/{enrollmentId}/pay
   * - PENDING → CONFIRMED 상태 전환
   */
  @PatchMapping("/{userId}/{enrollmentId}/pay")
  @Operation(summary = "결제 확정", description = "PENDING → CONFIRMED 상태 전환.")
  public ResponseEntity<ResultResponse> payEnroll(
      @Parameter(description = "수강생 ID", example = "4")
      @PathVariable Long userId,
      @PathVariable Long enrollmentId) {

    ResultResponse response = enrollmentService.payEnrollment(userId, enrollmentId);
    return new ResponseEntity<>(response, response.getStatus());
  }

  /**
   * 내 수강 신청 목록 조회
   * GET /api/v1/enrollment/{userId}?page=1
   * - 학생 전용
   * - 최신순 정렬, 페이지네이션 적용
   */
  @GetMapping("/{userId}")
  @Operation(summary = "내 수강 신청 목록 조회", description = "학생 전용. 최신순 정렬, 페이지네이션 적용.")
  public ResponseEntity<ResultResponse> getEnrollments(
      @PathVariable Long userId,
      @RequestParam(defaultValue = "1") int page) {

    ResultResponse response = enrollmentService.getEnrollments(userId, page);
    return new ResponseEntity<>(response, response.getStatus());
  }

  /**
   * 수강 취소
   * DELETE /api/v1/enrollment/{userId}/{enrollmentId}
   * - PENDING, WAITLISTED: 언제든 취소 가능
   * - CONFIRMED: 결제 후 7일 이내만 취소 가능
   * - 취소 시 대기자 자동 승격
   */
  @DeleteMapping("/{userId}/{enrollmentId}")
  @Operation(summary = "수강 취소", description = "결제 전 언제든 취소 가능. 결제 후 7일 이내 & 강의 시작 하루 전까지 취소 가능.")
  public ResponseEntity<ResultResponse> cancelEnroll(
      @PathVariable Long userId,
      @PathVariable Long enrollmentId) {

    ResultResponse response = enrollmentService.cancelEnrollment(userId, enrollmentId);
    return new ResponseEntity<>(response, response.getStatus());
  }
}