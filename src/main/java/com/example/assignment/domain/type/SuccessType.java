package com.example.assignment.domain.type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SuccessType {

  // =================== 강의 ===================
  SUCCESS_REGISTRATION_COURSE(HttpStatus.CREATED, "강의가 등록되었습니다."),
  SUCCESS_INQUIRY_COURSES(HttpStatus.OK, "강의 목록을 조회했습니다."),
  SUCCESS_INQUIRY_COURSES_DETAIL(HttpStatus.OK, "강의 상세 정보를 조회했습니다."),
  SUCCESS_UPDATE_COURSE_STATUS(HttpStatus.OK, "강의 상태가 변경되었습니다."),
  SUCCESS_INQUIRY_COURSE_ENROLLMENTS(HttpStatus.OK, "수강생 목록을 조회했습니다."),

  // =================== 수강 신청 ===================
  SUCCESS_ENROLLMENT(HttpStatus.CREATED, "수강신청이 완료되었습니다."),
  SUCCESS_WAITLISTED(HttpStatus.OK, "정원이 마감되어 대기 신청이 완료되었습니다."),
  SUCCESS_INQUIRY_ENROLLMENTS(HttpStatus.OK, "수강신청 목록을 조회했습니다."),
  SUCCESS_CANCEL_ENROLLMENT(HttpStatus.OK, "수강신청이 취소되었습니다."),

  // =================== 결제 ===================
  SUCCESS_PAYMENT(HttpStatus.OK, "결제가 완료되었습니다."),

  // =================== 정산 ===================
  SUCCESS_GET_SETTLEMENT(HttpStatus.OK, "정산 내역 조회에 성공했습니다."),
  SUCCESS_GET_ADMIN_SETTLEMENT(HttpStatus.OK, "정산 집계 내역 조회에 성공했습니다."),
  ;

  private final HttpStatus status;
  private final String message;
}