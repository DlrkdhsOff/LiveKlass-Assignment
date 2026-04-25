package com.example.assignment.domain.dto.response;

import com.example.assignment.domain.dto.PageResponse;
import com.example.assignment.domain.entity.Course;
import com.example.assignment.domain.entity.Enrollment;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CourseEnrollmentRes {

  private Long courseId;

  private String title;

  private String creatorName;

  private PageResponse<EnrollmentStudentRes> pageResponse;

  public static CourseEnrollmentRes of(Course course, List<Enrollment> enrollments, int page) {
    List<EnrollmentStudentRes> studentList = EnrollmentStudentRes.toList(enrollments);
    PageResponse<EnrollmentStudentRes> pageResponse = PageResponse.pagination(studentList, page);

    return new CourseEnrollmentRes(
        course.getCourseId(),
        course.getTitle(),
        course.getUser().getName(),
        pageResponse
    );
  }
}
