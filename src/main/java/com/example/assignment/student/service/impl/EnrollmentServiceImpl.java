package com.example.assignment.student.service.impl;

import com.example.assignment.common.dto.PageResponse;
import com.example.assignment.common.dto.ResultResponse;
import com.example.assignment.common.entity.User;
import com.example.assignment.common.exception.GlobalException;
import com.example.assignment.common.respotiroy.UserRepository;
import com.example.assignment.common.type.FailedType;
import com.example.assignment.common.type.SuccessType;
import com.example.assignment.creator.entity.Course;
import com.example.assignment.creator.entity.SaleRecord;
import com.example.assignment.creator.repository.CourseRepository;
import com.example.assignment.creator.repository.SaleRecordRepository;
import com.example.assignment.student.dto.EnrollmentPageRes;
import com.example.assignment.student.entity.Enrollment;
import com.example.assignment.student.repository.EnrollmentRepository;
import com.example.assignment.student.service.EnrollmentService;
import com.example.assignment.student.type.EnrollmentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EnrollmentServiceImpl implements EnrollmentService {

  private final EnrollmentRepository enrollmentRepository;
  private final CourseRepository courseRepository;
  private final UserRepository userRepository;
  private final SaleRecordRepository saleRecordRepository;

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
    Course course = getCourse(courseId);

    // TODO: 로그인 기능 및 인증/인가 로직 미구현으로 userId 를 직접 받는 방식으로 대체
    if (user.isCreator()) throw new GlobalException(FailedType.ACCESS_DENIED);
    validateEnrollable(course, user);

    if (course.isFull()) {
      enrollmentRepository.save(Enrollment.toEntity(course, user, EnrollmentStatus.WAITLISTED));
      return ResultResponse.of(SuccessType.SUCCESS_WAITLISTED);
    }

    enrollmentRepository.save(Enrollment.toEntity(course, user, EnrollmentStatus.PENDING));
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
    Enrollment enrollment = getEnrollmentWithCourse(enrollmentId);

    if (enrollment.isNotOwnedBy(userId)) throw new GlobalException(FailedType.ACCESS_DENIED);
    if (enrollment.isWaitlisted()) throw new GlobalException(FailedType.WAITLISTED_CANNOT_PAY);
    if (enrollment.isConfirmed()) throw new GlobalException(FailedType.ALREADY_PAID);
    if (enrollment.isCancelled()) throw new GlobalException(FailedType.ALREADY_CANCELLED);

    enrollment.confirm();
    saleRecordRepository.save(SaleRecord.toEntity(enrollment));

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
    if (user.isCreator()) throw new GlobalException(FailedType.ACCESS_DENIED);

    Pageable pageable = PageRequest.of(page - 1, 10);
    Page<Enrollment> enrollmentsPage = enrollmentRepository.findAllByUser(user, pageable);

    PageResponse<EnrollmentPageRes> enrollPageRes = new PageResponse<>(
        enrollmentsPage.getNumber() + 1,
        enrollmentsPage.getTotalPages(),
        enrollmentsPage.getTotalElements(),
        EnrollmentPageRes.toList(enrollmentsPage.getContent())
    );

    return new ResultResponse(SuccessType.SUCCESS_INQUIRY_ENROLLMENTS, enrollPageRes);
  }

  /**
   * 수강 취소
   * - 본인 수강 신청 건만 취소 가능
   * - 이미 취소된 건은 재취소 불가
   * - PENDING, WAITLISTED 는 언제든 취소 가능
   * - CONFIRMED 는 결제 후 7일 이내 AND 강의 시작 하루 전까지 두 조건 모두 만족 시 취소 가능
   * - WAITLISTED 취소 시 정원을 차지하지 않으므로 정원 복구 없이 상태만 변경
   * - PENDING, CONFIRMED 취소 시 정원 복구 후 강의가 OPEN 상태인 경우에만 대기자 자동 승격
   */
  @Override
  @Transactional
  public ResultResponse cancelEnrollment(Long userId, Long enrollmentId) {
    Enrollment enrollment = getEnrollmentWithCourse(enrollmentId);

    if (enrollment.isNotOwnedBy(userId)) throw new GlobalException(FailedType.ACCESS_DENIED);
    if (enrollment.isCancelled())        throw new GlobalException(FailedType.ALREADY_CANCELLED);
    if (!enrollment.isCancellable())     throw new GlobalException(FailedType.CANCEL_PERIOD_EXPIRED);

    switch (enrollment.getEnrollmentStatus()) {
      case WAITLISTED -> enrollment.cancel();
      case PENDING, CONFIRMED -> processCancelEnrollment(enrollment);
      default -> throw new GlobalException(FailedType.ALREADY_CANCELLED);
    }

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

  private Enrollment getEnrollmentWithCourse(Long enrollmentId) {
    return enrollmentRepository.findByIdWithCourse(enrollmentId)
        .orElseThrow(() -> new GlobalException(FailedType.ENROLLMENT_NOT_FOUND));
  }

  private SaleRecord getSaleRecord(Long enrollmentId) {
    return saleRecordRepository.findByEnrollment_EnrollmentId(enrollmentId)
        .orElseThrow(() -> new GlobalException(FailedType.SALE_RECORD_NOT_FOUND));
  }

  /**
   * 수강 신청 가능 여부 검증
   * - DRAFT 상태 강의 신청 불가
   * - 중복 신청 불가
   * - 시작일이 지난 강의 신청 불가
   */
  private void validateEnrollable(Course course, User user) {
    if (course.isNotAvailable()) throw new GlobalException(FailedType.COURSE_NOT_AVAILABLE);

    if (enrollmentRepository.existsActiveEnrollment(course, user, EnrollmentStatus.CANCELLED))
      throw new GlobalException(FailedType.ALREADY_ENROLLED);

    if (course.isStarted()) throw new GlobalException(FailedType.COURSE_ALREADY_STARTED);
  }

  /**
   * 수강 취소 처리
   * - CONFIRMED 취소 시 환불 처리
   * - 정원 복구 후 대기자 자동 승격
   */
  private void processCancelEnrollment(Enrollment enrollment) {
    Course course = getCourse(enrollment.getCourse().getCourseId());

    if (enrollment.isConfirmed()) {
      getSaleRecord(enrollment.getEnrollmentId()).cancel();
    }

    course.decreaseEnrollmentCnt();
    enrollment.cancel();

    if (course.isOpen()) {
      promoteNextFromWaitlist(course);
    }
  }

  private void promoteNextFromWaitlist(Course course) {
    enrollmentRepository.findFirstWaitlistedByCourse(course)
        .ifPresent(enrollment -> {
          enrollment.promote();
          course.increaseEnrollmentCnt();
        });
  }
}