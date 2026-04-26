package com.example.assignment.service.impl;

import com.example.assignment.domain.dto.PageResponse;
import com.example.assignment.domain.dto.ResultResponse;
import com.example.assignment.domain.dto.response.EnrollmentPageRes;
import com.example.assignment.domain.entity.Course;
import com.example.assignment.domain.entity.Enrollment;
import com.example.assignment.domain.entity.User;
import com.example.assignment.domain.repository.CourseRepository;
import com.example.assignment.domain.repository.EnrollmentRepository;
import com.example.assignment.domain.repository.UserRepository;
import com.example.assignment.domain.type.EnrollmentStatus;
import com.example.assignment.domain.type.FailedType;
import com.example.assignment.domain.type.SuccessType;
import com.example.assignment.exception.GlobalException;
import com.example.assignment.service.EnrollmentService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EnrollmentServiceImpl implements EnrollmentService {

  private final EnrollmentRepository enrollmentRepository;
  private final CourseRepository courseRepository;
  private final UserRepository userRepository;

  /**
   * 수강 신청
   * - 강사는 수강 신청 불가
   * - DRAFT 상태 강의는 신청 불가
   * - 이미 신청(PENDING, CONFIRMED, WAITLISTED)한 강의는 재신청 불가
   * - 시작일이 지난 강의는 신청 불가
   * - 정원이 꽉 찬 경우 대기열(WAITLISTED)에 등록
   */
  @Override
  @Transactional
  public ResultResponse enrollment(Long userId, Long courseId) {

    User user = getUser(userId);

    // TODO: 로그인 기능 및 인증/인가 로직 미구현으로 userId 를 직접 받는 방식으로 대체
    if (user.isCreator()) {
      throw new GlobalException(FailedType.ACCESS_DENIED);
    }

    Course course = getCourse(courseId);

    if (course.isNotAvailable()) {
      throw new GlobalException(FailedType.COURSE_NOT_AVAILABLE);
    }

    if (enrollmentRepository.existsByCourseAndUserAndEnrollmentStatusNot(
        course, user, EnrollmentStatus.CANCELLED)) {
      throw new GlobalException(FailedType.ALREADY_ENROLLED);
    }

    if (course.isStarted()) {
      throw new GlobalException(FailedType.COURSE_ALREADY_STARTED);
    }

    if (course.isFull()) {
      Enrollment waitlist = Enrollment.toEntity(course, user, EnrollmentStatus.WAITLISTED);
      enrollmentRepository.save(waitlist);

      return ResultResponse.of(SuccessType.SUCCESS_WAITLISTED);
    }

    Enrollment enrollment = Enrollment.toEntity(course, user, EnrollmentStatus.PENDING);
    enrollmentRepository.save(enrollment);
    course.increaseEnrollmentCnt();

    return ResultResponse.of(SuccessType.SUCCESS_ENROLLMENT);
  }

  /**
   * 결제 확정
   * - 본인 수강 신청 건만 결제 가능
   * - 이미 결제(CONFIRMED)된 건은 재결제 불가
   * - 취소(CANCELLED)된 건은 결제 불가
   * - 결제 완료 시 PENDING → CONFIRMED 상태 전환
   */
  @Override
  @Transactional
  public ResultResponse payEnrollment(Long userId, Long enrollmentId) {

    Enrollment enrollment = getEnrollment(enrollmentId);

    if (enrollment.isNotOwnedBy(userId)) {
      throw new GlobalException(FailedType.ACCESS_DENIED);
    }

    if (enrollment.isConfirmed()) {
      throw new GlobalException(FailedType.ALREADY_PAID);
    }

    if (enrollment.isCancelled()) {
      throw new GlobalException(FailedType.ALREADY_CANCELLED);
    }

    enrollment.confirm();

    return ResultResponse.of(SuccessType.SUCCESS_PAYMENT);
  }

  /**
   * 내 수강 신청 목록 조회
   * - 강사는 조회 불가 (학생 전용)
   * - 최신순 정렬
   * - 페이지네이션 적용 (기본 1페이지, 10개)
   */
  @Override
  @Transactional(readOnly = true)
  public ResultResponse getEnrollments(Long userId, int page) {

    User user = getUser(userId);

    // TODO: 로그인 기능 및 인증/인가 로직 미구현으로 userId 를 직접 받는 방식으로 대체
    if (user.isCreator()) {
      throw new GlobalException(FailedType.ACCESS_DENIED);
    }

    List<Enrollment> enrollments = enrollmentRepository.findAllByUserWithCourse(user);
    PageResponse<EnrollmentPageRes> enrollPageRes = EnrollmentPageRes.toList(enrollments, page);

    return new ResultResponse(SuccessType.SUCCESS_INQUIRY_ENROLLMENTS, enrollPageRes);
  }

  /**
   * 수강 취소
   * - 본인 수강 신청 건만 취소 가능
   * - 이미 취소된 건은 재취소 불가
   * - PENDING, WAITLISTED 는 언제든 취소 가능
   * - CONFIRMED 는 결제 후 7일 이내만 취소 가능
   * - WAITLISTED 취소 시 정원 복구 없이 상태만 변경
   * - PENDING, CONFIRMED 취소 시 정원 복구 후 대기자 자동 승격
   */
  @Override
  @Transactional
  public ResultResponse cancelEnrollment(Long userId, Long enrollmentId) {

    Enrollment enrollment = getEnrollmentWithCourse(enrollmentId);

    if (enrollment.isNotOwnedBy(userId)) {
      throw new GlobalException(FailedType.ACCESS_DENIED);
    }

    if (enrollment.isCancelled()) {
      throw new GlobalException(FailedType.ALREADY_CANCELLED);
    }

    if (!enrollment.isCancellable()) {
      throw new GlobalException(FailedType.CANCEL_PERIOD_EXPIRED);
    }

    if (enrollment.isWaitlisted()) {
      enrollment.cancel();
      return ResultResponse.of(SuccessType.SUCCESS_CANCEL_ENROLLMENT);
    }

    Course course = getCourse(enrollment.getCourse().getCourseId());

    course.decreaseEnrollmentCnt();
    enrollment.cancel();
    promoteNextFromWaitlist(course);

    return ResultResponse.of(SuccessType.SUCCESS_CANCEL_ENROLLMENT);
  }

  // =================== 내부 메서드 ===================

  private User getUser(Long userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new GlobalException(FailedType.USER_NOT_FOUND));
  }

  private Course getCourse(Long courseId) {
    return courseRepository.findByIdWithLock(courseId)
        .orElseThrow(() -> new GlobalException(FailedType.COURSE_NOT_FOUND));
  }

  private Enrollment getEnrollment(Long enrollmentId) {
    return enrollmentRepository.findById(enrollmentId)
        .orElseThrow(() -> new GlobalException(FailedType.ENROLLMENT_NOT_FOUND));
  }

  // course 포함 조회 (취소 시 사용)
  private Enrollment getEnrollmentWithCourse(Long enrollmentId) {
    return enrollmentRepository.findByIdWithCourse(enrollmentId)
        .orElseThrow(() -> new GlobalException(FailedType.ENROLLMENT_NOT_FOUND));
  }

  private void promoteNextFromWaitlist(Course course) {
    enrollmentRepository.findFirstWaitlistedByCourse(course)
        .ifPresent(enrollment -> {
          enrollment.promote();
          course.increaseEnrollmentCnt();
        });
  }
}