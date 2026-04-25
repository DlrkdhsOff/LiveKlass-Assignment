package com.example.assignment.domain.type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum FailedType {

  // =================== 사용자 ===================
  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."),
  ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),

  // =================== 강의 ===================
  COURSE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 강의입니다."),
  COURSE_IS_DUPLICATE(HttpStatus.CONFLICT, "이미 등록된 강의입니다."),
  COURSE_NOT_AVAILABLE(HttpStatus.BAD_REQUEST, "현재 수강신청이 가능한 강의가 아닙니다."),
  COURSE_IS_FULL(HttpStatus.BAD_REQUEST, "수강 정원이 마감되었습니다."),
  COURSE_ALREADY_STARTED(HttpStatus.BAD_REQUEST, "이미 시작된 강의는 신청할 수 없습니다."),
  COURSE_PERIOD_EXPIRED(HttpStatus.BAD_REQUEST, "수강 기간이 종료된 강의입니다."),

  // =================== 강의 상태 변경 ===================
  COURSE_ALREADY_OPEN(HttpStatus.BAD_REQUEST, "이미 모집 중인 강의입니다."),
  COURSE_ALREADY_CLOSED(HttpStatus.BAD_REQUEST, "이미 마감된 강의입니다."),
  INVALID_COURSE_STATUS_TRANSITION(HttpStatus.BAD_REQUEST, "해당 상태로는 변경할 수 없습니다."),

  // =================== 수강 신청 ===================
  ALREADY_ENROLLED(HttpStatus.CONFLICT, "이미 수강신청한 강의입니다."),
  ENROLLMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 수강신청 내역입니다."),
  CANCEL_PERIOD_EXPIRED(HttpStatus.BAD_REQUEST, "취소 가능 기간(결제 후 7일)이 지났습니다."),

  // =================== 결제 ===================
  ALREADY_PAID(HttpStatus.CONFLICT, "이미 결제가 완료된 수강신청입니다."),
  ALREADY_CANCELLED(HttpStatus.BAD_REQUEST, "이미 취소된 수강신청입니다."),

  // =================== 전역 예외 ===================
  INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "올바르지 않은 입력값입니다."),
  ;

  private final HttpStatus status;
  private final String message;
}