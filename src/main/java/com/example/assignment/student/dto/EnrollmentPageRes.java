package com.example.assignment.student.dto;

import com.example.assignment.common.dto.PageResponse;
import com.example.assignment.creator.entity.Course;
import com.example.assignment.student.entity.Enrollment;
import java.time.LocalDate;
import java.util.ArrayList;
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

  public static List<EnrollmentPageRes> toList(List<Enrollment> enrollments) {
    return enrollments.stream()
        .map(e -> {
          Course course = e.getCourse();
          return new EnrollmentPageRes(
              e.getEnrollmentId(),
              course.getCourseId(),
              course.getTitle(),
              course.getDescription(),
              course.getUser().getName(),
              course.getCourseStatus().getValue(),
              course.getStartPeriodAt(),
              course.getEndPeriodAt(),
              course.getEnrollmentCnt() + " / " + course.getPersonnel(),
              e.getEnrollmentStatus().getValue()
          );
        })
        .toList();
  }
}
