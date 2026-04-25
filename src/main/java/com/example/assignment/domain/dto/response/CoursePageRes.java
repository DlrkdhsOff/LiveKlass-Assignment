package com.example.assignment.domain.dto.response;

import com.example.assignment.domain.entity.Course;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CoursePageRes {

  private Long courseId;

  private String title;

  private String description;

  private String creatorName;

  private String courseStatus;

  private LocalDate startPeriodAt;

  private LocalDate endPeriodAt;

  public static List<CoursePageRes> toList(List<Course> list) {
    return list.stream()
        .map(course ->
            new CoursePageRes(
                course.getCourseId(),
                course.getTitle(),
                course.getDescription(),
                course.getUser().getName(),
                course.getCourseStatus().getValue(),
                course.getStartPeriodAt(),
                course.getEndPeriodAt()
            )
        )
        .toList();
  }
}
