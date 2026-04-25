package com.example.assignment.domain.dto.response;

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

  public static List<EnrollmentPageRes> toList(List<Enrollment> list) {
    return list.stream()
        .map(enrollment ->
            new EnrollmentPageRes(
                enrollment.getEnrollmentId(),
                enrollment.getCourse().getCourseId(),
                enrollment.getCourse().getTitle(),
                enrollment.getCourse().getDescription(),
                enrollment.getCourse().getUser().getName(),
                enrollment.getCourse().getCourseStatus().getValue(),
                enrollment.getCourse().getStartPeriodAt(),
                enrollment.getCourse().getEndPeriodAt(),
                enrollment.getCourse().getEnrollmentCnt() + " / " + enrollment.getCourse().getPersonnel(),
                enrollment.getEnrollmentStatus().getValue()
            )
        )
        .toList();
  }

}
