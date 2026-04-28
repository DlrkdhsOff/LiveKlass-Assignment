package com.example.assignment.student.dto;


import com.example.assignment.student.entity.Enrollment;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class StudentRes {

  private String studentName;

  private String enrollmentStatus;

  public static List<StudentRes> toList(List<Enrollment> list) {
    return list.stream()
        .map(enrollment ->
            new StudentRes(
                enrollment.getUser().getName(),
                enrollment.getEnrollmentStatus().getValue()
            ))
        .toList();
  }
}
