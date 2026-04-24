package com.example.assignment.service.impl;

import com.example.assignment.domain.dto.PageResponse;
import com.example.assignment.domain.dto.ResultResponse;
import com.example.assignment.domain.dto.request.CourseReq;
import com.example.assignment.domain.dto.response.CoursePageRes;
import com.example.assignment.domain.dto.response.CourseRes;
import com.example.assignment.domain.entity.Course;
import com.example.assignment.domain.entity.User;
import com.example.assignment.domain.repository.CourseRepository;
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

  @Override
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

    List<CoursePageRes> courseRes = CoursePageRes.toCourseResList(courses);

    PageResponse<CoursePageRes> pageResponse = PageResponse.pagination(courseRes, page);
    return new ResultResponse(SuccessType.SUCCESS_INQUIRY_COURSES, pageResponse);
  }

  @Override
  @Transactional(readOnly = true)
  public ResultResponse getCourseDetail(Long courseId) {

    Course course = courseRepository.findById(courseId)
        .orElseThrow(() -> new GlobalException(FailedType.COURSE_NOT_FOUND));

    CourseRes courseRes = CourseRes.toCourseRes(course);
    return new ResultResponse(SuccessType.SUCCESS_INQUIRY_COURSES_DETAIL, courseRes);
  }
}
