package com.example.assignment.service.impl;

import com.example.assignment.domain.dto.ResultResponse;
import com.example.assignment.domain.dto.request.CourseReq;
import com.example.assignment.domain.entity.Course;
import com.example.assignment.domain.entity.User;
import com.example.assignment.domain.repository.CourseRepository;
import com.example.assignment.domain.repository.UserRepository;
import com.example.assignment.domain.type.FailedType;
import com.example.assignment.domain.type.SuccessType;
import com.example.assignment.domain.type.UserRole;
import com.example.assignment.exception.GlobalException;
import com.example.assignment.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

  private final UserRepository userRepository;
  private final CourseRepository courseRepository;

  @Override
  public ResultResponse register(CourseReq courseReq) {

    User user = userRepository.findById(courseReq.getUserId())
        .orElseThrow(() -> new GlobalException(FailedType.USER_NOT_FOUND));

    // security 설정 없어서 임시로 설정
    if(user.getUserRole().equals(UserRole.STUDENT)) {
      throw new GlobalException(FailedType.ACCESS_DENIED);
    }

    boolean isDuplicate = courseRepository.existsDuplicateCourse(
        user,
        courseReq.getTitle(),
        courseReq.getDescription(),
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
}
