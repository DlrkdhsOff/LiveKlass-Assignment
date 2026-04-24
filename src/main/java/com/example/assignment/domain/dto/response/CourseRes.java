package com.example.assignment.domain.dto.response;

import com.example.assignment.domain.entity.Course;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CourseRes {

  private Long courseId;

  private String title;

  private String description;

  private String creatorName;

  private String courseStatus;

  private LocalDate startPeriodAt;

  private LocalDate endPeriodAt;

  private String amount;

  private String enrollmentRatio;

  public static CourseRes toCourseRes(Course course) {
    return new CourseRes(
        course.getCourseId(),
        course.getTitle(),
        course.getDescription(),
        course.getUser().getName(),
        course.getCourseStatus().getValue(),
        course.getStartPeriodAt(),
        course.getEndPeriodAt(),
        String.format("%,d원", course.getAmount()),
        course.getEnrollmentCnt() + " / " + course.getPersonnel()
    );
  }

}
