package com.example.assignment.domain.dto.response;

import com.example.assignment.domain.dto.PageResponse;
import com.example.assignment.domain.entity.Course;
import com.example.assignment.domain.entity.Enrollment;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class EnrollmentPageRes {

  private Long enrollmentId;

  private Long courseId;

  private String title;

  private String description;

  private String creatorName;

  private String courseStatus;

  private LocalDate startPeriodAt;

  private LocalDate endPeriodAt;

  private String enrollmentRatio;

  private String enrollmentStatus;

  public static PageResponse<EnrollmentPageRes> toList(List<Enrollment> list, int page) {
    List<EnrollmentPageRes> pageResList = list.stream()
        .map(EnrollmentPageRes::from)
        .toList();

    return PageResponse.pagination(pageResList, page);
  }

  private static EnrollmentPageRes from(Enrollment enrollment) {
    Course course = enrollment.getCourse();
    return new EnrollmentPageRes(
        enrollment.getEnrollmentId(),
        course.getCourseId(),
        course.getTitle(),
        course.getDescription(),
        course.getUser().getName(),
        course.getCourseStatus().getValue(),
        course.getStartPeriodAt(),
        course.getEndPeriodAt(),
        course.getEnrollmentCnt() + " / " + course.getPersonnel(),
        enrollment.getEnrollmentStatus().getValue()
    );
  }
}
