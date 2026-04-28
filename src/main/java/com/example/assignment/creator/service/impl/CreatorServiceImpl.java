package com.example.assignment.creator.service.impl;

import com.example.assignment.common.dto.PageResponse;
import com.example.assignment.common.dto.ResultResponse;
import com.example.assignment.common.entity.User;
import com.example.assignment.common.exception.GlobalException;
import com.example.assignment.common.respotiroy.UserRepository;
import com.example.assignment.common.type.FailedType;
import com.example.assignment.common.type.SuccessType;
import com.example.assignment.creator.dto.request.CourseReq;
import com.example.assignment.creator.dto.request.CourseStatusReq;
import com.example.assignment.creator.dto.response.CourseStudentRes;
import com.example.assignment.creator.dto.response.SettlementRes;
import com.example.assignment.creator.dto.response.SettlementSummary;
import com.example.assignment.creator.entity.Course;
import com.example.assignment.creator.entity.Settlement;
import com.example.assignment.creator.repository.CourseRepository;
import com.example.assignment.creator.repository.SettlementRepository;
import com.example.assignment.creator.repository.querydsl.CourseQuery;
import com.example.assignment.creator.repository.querydsl.SaleRecordQuery;
import com.example.assignment.creator.service.CreatorService;
import com.example.assignment.creator.type.CourseStatus;
import com.example.assignment.student.entity.Enrollment;
import com.example.assignment.student.repository.EnrollmentRepository;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreatorServiceImpl implements CreatorService {

  private final UserRepository userRepository;
  private final CourseRepository courseRepository;
  private final EnrollmentRepository enrollmentRepository;
  private final CourseQuery courseQuery;
  private final SaleRecordQuery saleRecordQuery;
  private final SettlementRepository settlementRepository;

  /**
   * 강의 등록
   */
  @Override
  public ResultResponse register(CourseReq courseReq) {
    User user = getUser(courseReq.getUserId());

    // TODO: 로그인 기능 및 인증/인가 로직 미구현으로 userId 를 직접 받는 방식으로 대체
    if (user.isStudent()) throw new GlobalException(FailedType.ACCESS_DENIED);

    if (isDuplicateCourse(user, courseReq)) {
      throw new GlobalException(FailedType.COURSE_IS_DUPLICATE);
    }

    courseRepository.save(Course.toEntity(courseReq, user));

    return ResultResponse.of(SuccessType.SUCCESS_REGISTRATION_COURSE);
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
    if (user.isStudent()) throw new GlobalException(FailedType.ACCESS_DENIED);

    Course course = getCourseWithLock(courseId);

    if (course.isNotOwnedBy(userId)) throw new GlobalException(FailedType.ACCESS_DENIED);
    if (course.isExpired()) throw new GlobalException(FailedType.COURSE_PERIOD_EXPIRED);

    updateStatus(course, courseStatusReq.getCourseStatus());

    return ResultResponse.of(SuccessType.SUCCESS_UPDATE_COURSE_STATUS);
  }

  /**
   * 강의별 수강생 목록 조회
   * - 페이지네이션 적용
   */
  @Override
  @Transactional(readOnly = true)
  public ResultResponse getCourseEnrollments(Long userId, int page) {
    User user = getUser(userId);

    // TODO: 로그인 기능 및 인증/인가 로직 미구현으로 userId 를 직접 받는 방식으로 대체
    if (user.isStudent()) throw new GlobalException(FailedType.ACCESS_DENIED);

    Pageable pageable = PageRequest.of(page - 1, 10);
    Page<Course> coursePage = courseRepository.findAllByUser(user, pageable);

    PageResponse<CourseStudentRes> response = new PageResponse<>(
        coursePage.getNumber() + 1,
        coursePage.getTotalPages(),
        coursePage.getTotalElements(),
        getCourseStudentList(coursePage)
    );

    return new ResultResponse(SuccessType.SUCCESS_INQUIRY_COURSE_ENROLLMENTS, response);
  }

  /**
   * 크리에이터 월별 정산 조회
   * 이미 저장된 정산 데이터가 있으면 반환, 없으면 집계 후 저장
   * PENDING 상태인 경우 취소 발생 여부를 반영하기 위해 재집계
   */
  @Override
  @Transactional
  public ResultResponse getSaleRecord(Long userId, String yearMonth) {
    User user = getUser(userId);

    // TODO: 로그인 기능 및 인증/인가 로직 미구현으로 userId 를 직접 받는 방식으로 대체
    if (user.isStudent()) throw new GlobalException(FailedType.ACCESS_DENIED);

    Settlement settlement = getSettlement(user, yearMonth);
    SettlementRes response = SettlementRes.toResponse(settlement);

    return new ResultResponse(SuccessType.SUCCESS_GET_SETTLEMENT, response);
  }

  // =================== 내부 메서드 ===================

  /**
   * 유저 조회
   * userId 에 해당하는 유저가 없으면 예외 발생
   */
  private User getUser(Long userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new GlobalException(FailedType.USER_NOT_FOUND));
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
    return courseQuery.existsDuplicateCourse(user, courseReq);
  }

  /**
   * 강의 상태 전환
   * OPEN → OPEN, CLOSED → OPEN, CLOSED → CLOSED 는 예외 발생
   */
  private void updateStatus(Course course, CourseStatus status) {
    switch (status) {
      case OPEN -> {
        if (course.isOpen()) throw new GlobalException(FailedType.COURSE_ALREADY_OPEN);
        if (course.isClosed()) throw new GlobalException(FailedType.COURSE_ALREADY_CLOSED);
        course.openCourse();
      }
      case CLOSED -> {
        if (course.isClosed()) throw new GlobalException(FailedType.COURSE_ALREADY_CLOSED);
        course.closeCourse();
      }
      default -> throw new GlobalException(FailedType.INVALID_COURSE_STATUS_TRANSITION);
    }
  }

  /**
   * 정산 데이터 조회
   * 1. 저장된 정산 데이터가 있는 경우
   *    - PENDING 상태: 취소 발생 가능성이 있으므로 재집계 후 업데이트
   *    - CONFIRMED / PAID 상태: 확정된 금액이므로 그대로 반환
   * 2. 저장된 정산 데이터가 없는 경우
   *    - 집계 쿼리 실행 후 PENDING 상태로 저장
   */
  private Settlement getSettlement(User user, String yearMonth) {
    YearMonth ym = getYearMonth(yearMonth);
    LocalDateTime startAt = ym.atDay(1).atStartOfDay();
    LocalDateTime endAt = ym.atEndOfMonth().atTime(23, 59, 59);

    return settlementRepository
        .findByUserAndSettlementMonth(user, ym.toString())
        .map(s -> {
          if (s.isPending()) {
            s.update(saleRecordQuery.getSummary(user, startAt, endAt));
          }
          return s;
        })
        .orElseGet(() -> {
          SettlementSummary summary = saleRecordQuery.getSummary(user, startAt, endAt);
          Settlement settlement = Settlement.toEntity(user, ym, summary);
          return settlementRepository.save(settlement);
        });
  }

  /**
   * 강의별 수강생 목록 매핑 (N+1 방지)
   * Course ID 목록으로 Enrollment 한 번에 조회 후 그룹핑
   */
  private List<CourseStudentRes> getCourseStudentList(Page<Course> coursePage) {
    List<Enrollment> enrollments = enrollmentRepository.findAllByCourse_CourseIdIn(
        coursePage.getContent().stream()
            .map(Course::getCourseId)
            .toList());

    Map<Long, List<Enrollment>> enrollmentMap = enrollments.stream()
        .collect(Collectors.groupingBy(e -> e.getCourse().getCourseId()));

    return coursePage.getContent().stream()
        .map(course -> CourseStudentRes.of(course,
            enrollmentMap.getOrDefault(course.getCourseId(), List.of())))
        .toList();
  }

  /**
   * 연월 파싱 및 검증
   * - null 이면 현재 연월 사용
   * - 미래 날짜면 예외 발생
   * - 형식이 올바르지 않으면 예외 발생 (올바른 형식: 2025-03)
   */
  private YearMonth getYearMonth(String yearMonth) {
    try {
      YearMonth ym = (yearMonth == null) ? YearMonth.now() : YearMonth.parse(yearMonth);
      if (ym.isAfter(YearMonth.now())) throw new GlobalException(FailedType.INVALID_YEAR_MONTH);
      return ym;
    } catch (DateTimeParseException e) {
      throw new GlobalException(FailedType.INVALID_DATE_YEAR_MONTH_FORMAT);
    }
  }
}