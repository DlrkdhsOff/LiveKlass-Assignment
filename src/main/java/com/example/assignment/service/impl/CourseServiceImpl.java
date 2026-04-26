package com.example.assignment.service.impl;

import com.example.assignment.domain.dto.PageResponse;
import com.example.assignment.domain.dto.ResultResponse;
import com.example.assignment.domain.dto.request.CourseReq;
import com.example.assignment.domain.dto.request.CourseSearchReq;
import com.example.assignment.domain.dto.request.CourseStatusReq;
import com.example.assignment.domain.dto.response.CourseEnrollmentRes;
import com.example.assignment.domain.dto.response.CoursePageRes;
import com.example.assignment.domain.dto.response.CourseRes;
import com.example.assignment.domain.entity.Course;
import com.example.assignment.domain.entity.Enrollment;
import com.example.assignment.domain.entity.User;
import com.example.assignment.domain.repository.CourseRepository;
import com.example.assignment.domain.repository.EnrollmentRepository;
import com.example.assignment.domain.repository.UserRepository;
import com.example.assignment.domain.repository.querydsl.CourseQueryRepository;
import com.example.assignment.domain.type.CourseStatus;
import com.example.assignment.domain.type.FailedType;
import com.example.assignment.domain.type.SuccessType;
import com.example.assignment.exception.GlobalException;
import com.example.assignment.service.CourseService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

  private final UserRepository userRepository;
  private final CourseRepository courseRepository;
  private final CourseQueryRepository courseQueryRepository;
  private final EnrollmentRepository enrollmentRepository;

  /**
   * 강의 등록
   * - 모든 필드가 동일한 강의 중복 등록 불가
   */
  @Override
  @Transactional
  public ResultResponse register(CourseReq courseReq) {

    User user = getUser(courseReq.getUserId());

    // TODO: 로그인 기능 및 인증/인가 로직 미구현으로 userId 를 직접 받는 방식으로 대체
    if (user.isStudent()) throw new GlobalException(FailedType.ACCESS_DENIED);

    if (isDuplicateCourse(user, courseReq)) throw new GlobalException(FailedType.COURSE_IS_DUPLICATE);

    courseRepository.save(Course.toEntity(courseReq, user));

    return ResultResponse.of(SuccessType.SUCCESS_REGISTRATION_COURSE);
  }

  /**
   * 강의 목록 조회
   * - 모든 조건은 선택값 (없으면 전체 조회)
   * - 강사명, 제목, 가격 범위, 기간, 상태로 필터링 가능
   * - 페이지네이션 적용 (기본 1페이지)
   */
  @Override
  @Transactional(readOnly = true)
  public ResultResponse getCourses(CourseSearchReq searchReq, int page) {

    List<Course> courses = courseQueryRepository.searchCourses(searchReq);

    PageResponse<CoursePageRes> response = CoursePageRes.toList(courses, page);

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
    CourseRes response = CourseRes.of(course);

    return new ResultResponse(SuccessType.SUCCESS_INQUIRY_COURSES_DETAIL, response);
  }

  /**
   * 강의 상태 변경
   * - 수강 기간 만료 시 변경 불가
   * - 허용 전환: DRAFT → OPEN, DRAFT → CLOSED, OPEN → CLOSED
   * - 불가 전환: CLOSED → OPEN, CLOSED → CLOSED, OPEN → OPEN
   * - 조기 마감 시 동시성 방지를 위해 비관적 락 적용
   */
  @Override
  @Transactional
  public ResultResponse updateCourseStatus(Long userId, Long courseId, CourseStatusReq courseStatusReq) {

    User user = getUser(userId);

    // TODO: 로그인 기능 및 인증/인가 로직 미구현으로 userId 를 직접 받는 방식으로 대체
    if (user.isStudent()) {
      throw new GlobalException(FailedType.ACCESS_DENIED);
    }

    Course course = getCourseWithLock(courseId);

    if (course.isNotOwnedBy(userId)) {
      throw new GlobalException(FailedType.ACCESS_DENIED);
    }

    if (course.isExpired()) {
      throw new GlobalException(FailedType.COURSE_PERIOD_EXPIRED);
    }

    CourseStatus status = courseStatusReq.getCourseStatus();

    if (status == CourseStatus.OPEN) {
      if (course.isOpen()) throw new GlobalException(FailedType.COURSE_ALREADY_OPEN);
      if (course.isClosed()) throw new GlobalException(FailedType.COURSE_ALREADY_CLOSED);
      course.openCourse();

    } else if (status == CourseStatus.CLOSED) {
      if (course.isClosed()) throw new GlobalException(FailedType.COURSE_ALREADY_CLOSED);
      course.closeCourse();

    } else {
      throw new GlobalException(FailedType.INVALID_COURSE_STATUS_TRANSITION);
    }

    return ResultResponse.of(SuccessType.SUCCESS_UPDATE_COURSE_STATUS);
  }

  // =================== 수강생 목록 조회 ===================

  /**
   * 강의별 수강생 목록 조회
   * - 최신순 정렬, 페이지네이션 적용
   */
  @Override
  @Transactional(readOnly = true)
  public ResultResponse getCourseEnrollments(Long userId, Long courseId, int page) {

    User user = getUser(userId);

    // TODO: 로그인 기능 및 인증/인가 로직 미구현으로 userId 를 직접 받는 방식으로 대체
    if (user.isStudent()) {
      throw new GlobalException(FailedType.ACCESS_DENIED);
    }

    Course course = getCourse(courseId);

    if (course.isNotOwnedBy(userId)) {
      throw new GlobalException(FailedType.ACCESS_DENIED);
    }

    List<Enrollment> enrollments = enrollmentRepository.findAllByCourseWithUser(course);
    CourseEnrollmentRes response = CourseEnrollmentRes.of(course, enrollments, page);

    return new ResultResponse(SuccessType.SUCCESS_INQUIRY_COURSE_ENROLLMENTS, response);
  }

  // =================== 내부 메서드 ===================

  private User getUser(Long userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new GlobalException(FailedType.USER_NOT_FOUND));
  }

  private Course getCourse(Long courseId) {
    return courseRepository.findById(courseId)
        .orElseThrow(() -> new GlobalException(FailedType.COURSE_NOT_FOUND));
  }

  /**
   * courseId 로 Course 조회 (비관적 락 적용)
   * 강의 상태 변경 시 동시 수강 신청과의 경합 방지
   * 존재하지 않으면 COURSE_NOT_FOUND 예외 발생
   */
  private Course getCourseWithLock(Long courseId) {
    return courseRepository.findByIdWithLock(courseId)
        .orElseThrow(() -> new GlobalException(FailedType.COURSE_NOT_FOUND));
  }

  /**
   * 강의 중복 여부 확인
   * 동일 강사가 모든 필드가 같은 강의를 등록하는 경우 중복으로 판단
   */
  private boolean isDuplicateCourse(User user, CourseReq courseReq) {
    return courseQueryRepository.existsDuplicateCourse(user, courseReq);
  }
}