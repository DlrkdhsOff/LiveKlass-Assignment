package com.example.assignment.domain.dto.request;

import com.example.assignment.domain.type.CourseStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CourseSearchReq {

  @Schema(example = "강사1")
  private String creatorName;

  @Schema(example = "Spring Boot")
  private String title;

  @Schema(example = "30000")
  private Long minAmount;

  @Schema(example = "80000")
  private Long maxAmount;

  @Schema(example = "2026-05-01")
  private LocalDate startPeriodAt;

  @Schema(example = "2026-07-31")
  private LocalDate endPeriodAt;

  @Schema(example = "OPEN")
  private CourseStatus courseStatus;

}