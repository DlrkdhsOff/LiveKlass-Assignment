package com.example.assignment.domain.type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SuccessType {
  SUCCESS_REGISTRATION_COURSE(HttpStatus.OK, "강의를 성공적으로 등록하였습니다."),
  SUCCESS_INQUIRY_COURSES(HttpStatus.OK, "강의목록을 성공적으로 조회하였습니다."),
  SUCCESS_INQUIRY_COURSES_DETAIL(HttpStatus.OK, "강의 상세 정보를 성공적으로 조회하였습니다."),
  ;

  private final HttpStatus status;
  private final String message;
}
