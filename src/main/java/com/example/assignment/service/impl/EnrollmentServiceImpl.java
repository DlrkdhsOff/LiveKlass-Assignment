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

  // =================== 수강 신청 ===================

  @Override
  @Transactional
  public ResultResponse enroll(Long userId, Long courseId) {

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new GlobalException(FailedType.USER_NOT_FOUND));

    if (user.isCreator()) {
      throw new GlobalException(FailedType.ACCESS_DENIED);
    }

    Course course = courseRepository.findByIdWithLock(courseId)
        .orElseThrow(() -> new GlobalException(FailedType.COURSE_NOT_FOUND));

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
      Enrollment waitlist = Enrollment.toWaitlistEntity(course, user);
      enrollmentRepository.save(waitlist);
      return ResultResponse.of(SuccessType.SUCCESS_WAITLISTED);
    }

    Enrollment enrollment = Enrollment.toEntity(course, user);
    enrollmentRepository.save(enrollment);
    course.increaseEnrollmentCnt();

    return ResultResponse.of(SuccessType.SUCCESS_ENROLLMENT);
  }

  // =================== 결제 ===================

  @Override
  @Transactional
  public ResultResponse payEnroll(Long userId, Long enrollmentId) {

    Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
        .orElseThrow(() -> new GlobalException(FailedType.ENROLLMENT_NOT_FOUND));

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

  // =================== 조회 ===================

  @Override
  @Transactional(readOnly = true)
  public ResultResponse getEnroll(Long userId, int page) {

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new GlobalException(FailedType.USER_NOT_FOUND));

    List<Enrollment> enrollments = enrollmentRepository.findAllByUserWithCourse(user);
    List<EnrollmentPageRes> enrollmentPageRes = EnrollmentPageRes.toList(enrollments);
    PageResponse<EnrollmentPageRes> enrollPageRes = PageResponse.pagination(enrollmentPageRes, page);

    return new ResultResponse(SuccessType.SUCCESS_INQUIRY_ENROLLMENTS, enrollPageRes);
  }

  // =================== 취소 ===================

  @Override
  @Transactional
  public ResultResponse cancelEnroll(Long userId, Long enrollmentId) {

    Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
        .orElseThrow(() -> new GlobalException(FailedType.ENROLLMENT_NOT_FOUND));

    if (enrollment.isNotOwnedBy(userId)) {
      throw new GlobalException(FailedType.ACCESS_DENIED);
    }

    if (enrollment.isCancelled()) {
      throw new GlobalException(FailedType.ALREADY_CANCELLED);
    }

    if (!enrollment.isCancellable()) {
      throw new GlobalException(FailedType.CANCEL_PERIOD_EXPIRED);
    }

    // 대기 중 취소는 정원 복구 불필요
    if (enrollment.isWaitlisted()) {
      enrollment.cancel();
      return ResultResponse.of(SuccessType.SUCCESS_CANCEL_ENROLLMENT);
    }

    Course course = courseRepository.findByIdWithLock(enrollment.getCourse().getCourseId())
        .orElseThrow(() -> new GlobalException(FailedType.COURSE_NOT_FOUND));

    course.decreaseEnrollmentCnt();
    enrollment.cancel();
    promoteNextFromWaitlist(course);

    return ResultResponse.of(SuccessType.SUCCESS_CANCEL_ENROLLMENT);
  }

  // =================== 내부 메서드 ===================

  private void promoteNextFromWaitlist(Course course) {
    enrollmentRepository.findFirstWaitlistedByCourse(course)
        .ifPresent(e -> {
          e.promote();
          course.increaseEnrollmentCnt();
        });
  }
}