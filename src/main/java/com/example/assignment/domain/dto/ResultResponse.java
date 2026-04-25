package com.example.assignment.domain.dto;

import com.example.assignment.domain.type.FailedType;
import com.example.assignment.domain.type.SuccessType;
import lombok.Getter;
import org.springframework.http.HttpStatus;


@Getter
public class ResultResponse {

  private final Integer code;
  private final HttpStatus status;
  private final String message;
  private final Object data;

  // 요청 응답 시 반환할 데이터가 존재 할 경우
  public ResultResponse(SuccessType successResultCode, Object data) {
    this.code = successResultCode.getStatus().value();
    this.status = successResultCode.getStatus();
    this.message = successResultCode.getMessage();
    this.data = data;
  }

  public ResultResponse(FailedType failedResultCode,  Object data) {
    this.code = failedResultCode.getStatus().value();
    this.status = failedResultCode.getStatus();
    this.message = failedResultCode.getMessage();
    this.data = data;
  }

  public ResultResponse(HttpStatus status, String message) {
    this.code = status.value();
    this.status = status;
    this.message = message;
    this.data = null;
  }

  // 요청 응답 시 반환할 데이터가 존재하지 않을 경우
  public static ResultResponse of(SuccessType successResultCode) {
    return new ResultResponse(successResultCode, null);
  }

  public static ResultResponse of(FailedType failedResultCode) {
    return new ResultResponse(failedResultCode, null);
  }
}
