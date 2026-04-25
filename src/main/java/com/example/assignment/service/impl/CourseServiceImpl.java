package com.example.assignment.service.impl;

import com.example.assignment.domain.dto.PageResponse;
import com.example.assignment.domain.dto.ResultResponse;
import com.example.assignment.domain.dto.request.CourseReq;
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
import java.time.LocalDate;
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

  @Override
  @Transactional
  public ResultResponse register(CourseReq courseReq) {

    User user = userRepository.findById(courseReq.getUserId())
        .orElseThrow(() -> new GlobalException(FailedType.USER_NOT_FOUND));

    // security 설정 없어서 임시로 설정
    if (user.isStudent()) {
      throw new GlobalException(FailedType.ACCESS_DENIED);
    }

    boolean isDuplicate = courseRepository.existsDuplicateCourse(
        user,
        courseReq.getTitle(),
        courseReq.getDescription(),
        courseReq.getAmount(),
        courseReq.getPersonnel(),
        courseReq.getStartPeriodAt(),
        courseReq.getEndPeriodAt(),
        courseReq.getCourseStatus()
    );

    if(isDuplicate) throw new GlobalException(FailedType.COURSE_IS_DUPLICATE);

    Course course = Course.toEntity(courseReq, user);

    courseRepository.save(course);

    return ResultResponse.of(SuccessType.SUCCESS_REGISTRATION_COURSE);
  }

  @Override
  @Transactional(readOnly = true)
  public ResultResponse getCourses(String creatorName, String title, Long minAmount, Long maxAmount,
      LocalDate startPeriodAt, LocalDate endPeriodAt, CourseStatus courseStatus, int page) {

    List<Course> courses = courseQueryRepository.searchCourses(creatorName, title, minAmount,
        maxAmount, startPeriodAt, endPeriodAt, courseStatus);

    List<CoursePageRes> courseRes = CoursePageRes.toList(courses);

    PageResponse<CoursePageRes> pageResponse = PageResponse.pagination(courseRes, page);
    return new ResultResponse(SuccessType.SUCCESS_INQUIRY_COURSES, pageResponse);
  }

  @Override
  @Transactional(readOnly = true)
  public ResultResponse getCourseDetail(Long courseId) {

    Course course = courseRepository.findById(courseId)
        .orElseThrow(() -> new GlobalException(FailedType.COURSE_NOT_FOUND));

    CourseRes courseRes = CourseRes.of(course);
    return new ResultResponse(SuccessType.SUCCESS_INQUIRY_COURSES_DETAIL, courseRes);
  }

  @Override
  @Transactional
  public ResultResponse updateCourseStatus(Long userId, Long courseId, CourseStatusReq courseStatusReq) {

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new GlobalException(FailedType.USER_NOT_FOUND));

    if (user.isStudent()) {
      throw new GlobalException(FailedType.ACCESS_DENIED);
    }

    Course course = courseRepository.findByIdWithLock(courseId)
        .orElseThrow(() -> new GlobalException(FailedType.COURSE_NOT_FOUND));

    if (course.isNotOwnedBy(userId)) {
      throw new GlobalException(FailedType.ACCESS_DENIED);
    }

    if(course.isExpired()) {
      throw new GlobalException(FailedType.COURSE_PERIOD_EXPIRED);
    }

    if(courseStatusReq.getCourseStatus() == CourseStatus.OPEN) {
      if (course.isOpen()) throw new GlobalException(FailedType.COURSE_ALREADY_OPEN);
      if (course.isClosed()) throw new GlobalException(FailedType.COURSE_ALREADY_CLOSED);
      course.openCourse();

    } else if(courseStatusReq.getCourseStatus() == CourseStatus.CLOSED) {
      if (course.isClosed()) throw new GlobalException(FailedType.COURSE_ALREADY_CLOSED);

      course.closeCourse();
    } else {
      throw new GlobalException(FailedType.INVALID_COURSE_STATUS_TRANSITION);
    }

    return ResultResponse.of(SuccessType.SUCCESS_UPDATE_COURSE_STATUS);
  }

  @Override
  @Transactional(readOnly = true)
  public ResultResponse getCourseEnrollments(Long userId, Long courseId, int page) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new GlobalException(FailedType.USER_NOT_FOUND));

    if (user.isStudent()) {
      throw new GlobalException(FailedType.ACCESS_DENIED);
    }

    Course course = courseRepository.findById(courseId)
        .orElseThrow(() -> new GlobalException(FailedType.COURSE_NOT_FOUND));

    if (course.isNotOwnedBy(userId)) {
      throw new GlobalException(FailedType.ACCESS_DENIED);
    }

    List<Enrollment> enrollments = enrollmentRepository.findAllByCourseWithUser(course);
    CourseEnrollmentRes response = CourseEnrollmentRes.of(course, enrollments, page);

    return new ResultResponse(SuccessType.SUCCESS_INQUIRY_COURSE_ENROLLMENTS, response);
  }
}
