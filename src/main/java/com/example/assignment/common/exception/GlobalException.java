package com.example.assignment.common.exception;

import com.example.assignment.common.dto.ResultResponse;
import com.example.assignment.common.type.FailedType;
import lombok.Getter;

@Getter
public class GlobalException extends RuntimeException {

  private final ResultResponse resultResponse;

  public GlobalException(FailedType failedType) {
    super("");
    this.resultResponse = ResultResponse.of(failedType);
  }

  public GlobalException(ResultResponse resultResponse) {
    super("");
    this.resultResponse = resultResponse;
  }

}
