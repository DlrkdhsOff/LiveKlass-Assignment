package com.example.assignment.domain.dto.request;

import com.example.assignment.domain.type.CourseStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CourseStatusReq {

  @NotNull(message = "변경할 강의 상태를 입력해주세요.")
  private CourseStatus courseStatus;
}
