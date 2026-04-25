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
  COURSE_NOT_AVAILABLE(HttpStatus.BAD_REQUEST, "현재 수강신청이 불가능한 강의입니다."),
  COURSE_IS_FULL(HttpStatus.BAD_REQUEST, "수강 정원이 마감되었습니다."),
  ALREADY_ENROLLED(HttpStatus.CONFLICT, "이미 수강신청한 강의입니다."),
  ENROLLMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 수강신청 내역입니다."),
  ALREADY_PAID(HttpStatus.BAD_REQUEST, "이미 결제가 완료된 수강신청입니다."),
  ALREADY_CANCELLED(HttpStatus.BAD_REQUEST, "이미 취소된 수강신청입니다."),
  COURSE_PERIOD_EXPIRED(HttpStatus.BAD_REQUEST, "수강 기간이 만료된 강의입니다."),
  COURSE_ALREADY_OPEN(HttpStatus.BAD_REQUEST, "이미 모집 중인 강의입니다."),
  COURSE_ALREADY_CLOSED(HttpStatus.BAD_REQUEST, "이미 마감된 강의입니다."),
  ;

  private final HttpStatus status;
  private final String message;
}