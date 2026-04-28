package com.example.assignment.creator.dto.response;

import com.example.assignment.creator.entity.Course;
import com.example.assignment.student.dto.StudentRes;
import com.example.assignment.student.entity.Enrollment;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CourseStudentRes {

  private Long courseId;

  private String title;

  private List<StudentRes> list;

  public static CourseStudentRes of(Course course, List<Enrollment> enrollments) {
    List<StudentRes> studentRes = StudentRes.toList(enrollments);

    return new CourseStudentRes(
        course.getCourseId(),
        course.getTitle(),
        studentRes
    );
  }
}
