package com.example.assignment.domain.dto.response;


import com.example.assignment.domain.entity.Enrollment;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class EnrollmentStudentRes {

  private String studentName;

  private String enrollmentStatus;

  public static List<EnrollmentStudentRes> toList(List<Enrollment> list) {
    return list.stream()
        .map(enrollment ->
            new EnrollmentStudentRes(
                enrollment.getUser().getName(),
                enrollment.getEnrollmentStatus().getValue()
            ))
        .toList();
  }
}
