package com.example.assignment.creator.controller;

import com.example.assignment.common.dto.ResultResponse;
import com.example.assignment.creator.dto.request.CourseSearchReq;
import com.example.assignment.creator.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
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
}
