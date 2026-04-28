package com.example.assignment.creator.dto.response;

import com.example.assignment.creator.entity.Course;
import java.util.List;
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

  private String courseStatus;

  private String amount;

  private String enrollmentRatio;

  public static List<CourseRes> toList(List<Course> list) {
    return list.stream()
        .map(course ->
            new CourseRes(
                course.getCourseId(),
                course.getTitle(),
                course.getDescription(),
                course.getCourseStatus().getValue(),
                String.format("%,d원", course.getAmount()),
                course.getEnrollmentCnt() + " / " + course.getPersonnel()
            )
        )
        .toList();
  }
}
