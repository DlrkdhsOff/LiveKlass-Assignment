package com.example.assignment.creator.controller;

import com.example.assignment.common.dto.ResultResponse;
import com.example.assignment.creator.dto.request.CourseReq;
import com.example.assignment.creator.dto.request.CourseStatusReq;
import com.example.assignment.creator.service.CreatorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

@Tag(name = "강사 API", description = "정산 집계 내역 조회")
@RestController
@RequestMapping("/api/v1/creator")
@RequiredArgsConstructor
public class CreatorController {

  private final CreatorService creatorService;

  /**
   * 강의 등록
   * POST /api/v1/course
   * - 강사 전용
   * - 동일한 강의 중복 등록 불가
   */
  @PostMapping
  @Operation(summary = "강의 등록", description = "강사 전용. 동일한 강의 중복 등록 불가.")
  public ResponseEntity<ResultResponse> register(@RequestBody @Valid CourseReq courseReq) {

    ResultResponse response = creatorService.register(courseReq);
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

    ResultResponse response = creatorService.updateCourseStatus(userId, courseId, courseStatusReq);
    return new ResponseEntity<>(response, response.getStatus());
  }

  /**
   * 강의별 수강생 목록 조회
   * GET /api/v1/course/{userId}/{courseId}/enrollments
   * - 강사 전용 (본인 강의만 조회 가능)
   * - 최신순 정렬, 페이지네이션 적용
   */
  @GetMapping("/{userId}/enrollments")
  @Operation(summary = "강의별 수강생 목록 조회", description = "강사 전용. 본인 강의만 조회 가능. 최신순 정렬.")
  public ResponseEntity<ResultResponse> getCourseEnrollments(
      @PathVariable Long userId,
      @RequestParam(defaultValue = "1") int page) {

    ResultResponse response = creatorService.getCourseEnrollments(userId, page);
    return new ResponseEntity<>(response, response.getStatus());
  }

  @GetMapping("/{userId}/sale")
  @Operation(summary = "월 별 정산 집계 내역 조회", description = "기간, 상태로 필터링 가능.")
  public ResponseEntity<ResultResponse> getSaleRecord(
      @PathVariable Long userId,
      @RequestParam(required = false) String yearMonth) {

    ResultResponse response = creatorService.getSaleRecord(userId, yearMonth);
    return new ResponseEntity<>(response, response.getStatus());
  }

}
