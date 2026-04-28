package com.example.assignment.creator.service.impl;

import com.example.assignment.common.dto.PageResponse;
import com.example.assignment.common.dto.ResultResponse;
import com.example.assignment.common.exception.GlobalException;
import com.example.assignment.common.type.FailedType;
import com.example.assignment.common.type.SuccessType;
import com.example.assignment.creator.dto.request.CourseSearchReq;
import com.example.assignment.creator.dto.response.CourseDetailsRes;
import com.example.assignment.creator.dto.response.CourseRes;
import com.example.assignment.creator.entity.Course;
import com.example.assignment.creator.repository.CourseRepository;
import com.example.assignment.creator.repository.querydsl.CourseQuery;
import com.example.assignment.creator.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

  private final CourseRepository courseRepository;
  private final CourseQuery courseQuery;

  /**
   * 강의 목록 조회
   * - 모든 조건은 선택값 (없으면 전체 조회)
   * - 강사명, 제목, 가격 범위, 기간, 상태로 필터링 가능
   * - 페이지네이션 적용 (기본 1페이지)
   */
  @Override
  @Transactional(readOnly = true)
  public ResultResponse getCourses(CourseSearchReq searchReq, int page) {

    Pageable pageable = PageRequest.of(page - 1, 10);
    Page<Course> coursePage = courseQuery.searchCourses(searchReq, pageable);

    PageResponse<CourseRes> response = new PageResponse<>(
        coursePage.getNumber() + 1,
        coursePage.getTotalPages(),
        coursePage.getTotalElements(),
        CourseRes.toList(coursePage.getContent())
    );

    return new ResultResponse(SuccessType.SUCCESS_INQUIRY_COURSES, response);
  }

  /**
   * 강의 상세 조회
   * - 현재 수강 신청 인원 포함
   */
  @Override
  @Transactional(readOnly = true)
  public ResultResponse getCourseDetail(Long courseId) {

    Course course = getCourse(courseId);
    CourseDetailsRes response = CourseDetailsRes.of(course);

    return new ResultResponse(SuccessType.SUCCESS_INQUIRY_COURSES_DETAIL, response);
  }


  // =================== 내부 메서드 ===================

  private Course getCourse(Long courseId) {
    return courseRepository.findById(courseId)
        .orElseThrow(() -> new GlobalException(FailedType.COURSE_NOT_FOUND));
  }

}