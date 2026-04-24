package com.example.assignment.domain.dto.request;

import com.example.assignment.domain.type.CourseStatus;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CourseSearchReq {

  private String creatorName;

  private String title;

  private Long minAmount;

  private Long maxAmount;

  private LocalDate startPeriodAt;

  private LocalDate endPeriodAt;

  private CourseStatus courseStatus;

}