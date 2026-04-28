package com.example.assignment.creator.dto.request;

import com.example.assignment.creator.type.CourseStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CourseStatusReq {

  @Schema(example = "OPEN")
  @NotNull(message = "변경할 강의 상태를 입력해주세요.")
  private CourseStatus courseStatus;
}
