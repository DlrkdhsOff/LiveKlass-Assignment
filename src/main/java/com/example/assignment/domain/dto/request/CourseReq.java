package com.example.assignment.domain.dto.request;

import com.example.assignment.domain.type.CourseStatus;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Getter;

@Getter
public class CourseReq {

  @NotNull(message = "강사ID를 입력해주세요.")
  private Long userId;

  @NotBlank(message = "제목을 입력해주세요.")
  private String title;

  @NotBlank(message = "설명을 입력해주세요")
  private String description;

  @NotNull(message = "강의 가격을 입력해주세요.")
  private Long amount;

  @NotNull(message = "최대 수강 인원을 입력해주세요.")
  @Min(value = 5, message = "최대 수강 인원은 5명 이상이어야 합니다.")
  private Long personnel;

  @NotNull(message = "시작일을 입력해주세요")
  @FutureOrPresent(message = "시작일은 오늘 또는 이후로 선택해주세요.")
  private LocalDate startPeriodAt;

  @NotNull(message = "종료일을 입력해주세요")
  private LocalDate endPeriodAt;

  @NotNull(message = "강의 상태를 입력해주세요")
  private CourseStatus courseStatus;

  @AssertTrue(message = "종료일은 시작일 이후여야 합니다.")
  public boolean isValidPeriod() {
    if (startPeriodAt == null || endPeriodAt == null) return true;
    return endPeriodAt.isAfter(startPeriodAt);
  }

}
