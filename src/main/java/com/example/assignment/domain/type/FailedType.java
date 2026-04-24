package com.example.assignment.domain.type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum FailedType {
  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."),
  ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 작업에 대한 권한이 없습니다."),
  COURSE_IS_DUPLICATE(HttpStatus.BAD_REQUEST, "이미 등록한 강의 입니다."),
  COURSE_NOT_FOUND(HttpStatus.BAD_REQUEST, "존재하지 않는 강의 입니다.."),
  ;

  private final HttpStatus status;
  private final String message;
}