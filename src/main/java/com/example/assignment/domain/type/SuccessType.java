package com.example.assignment.domain.type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SuccessType {
  SUCCESS_REGISTRATION_COURSE(HttpStatus.OK, "강의를 성공적으로 등록하였습니다."),
  SUCCESS_INQUIRY_COURSES(HttpStatus.OK, "강의 목록을 성공적으로 조회하였습니다."),
  SUCCESS_INQUIRY_COURSES_DETAIL(HttpStatus.OK, "강의 상세 정보를 성공적으로 조회하였습니다."),
  SUCCESS_ENROLLMENT(HttpStatus.OK, "수강신청이 성공적으로 완료되었습니다."),
  SUCCESS_PAYMENT(HttpStatus.OK, "결제가 성공적으로 완료되었습니다."),
  SUCCESS_INQUIRY_ENROLLMENTS(HttpStatus.OK, "내 수강 신청 목록을 성공적으로 조회하였습니다."),
  SUCCESS_CANCEL_ENROLLMENT(HttpStatus.OK, "신청한 수강을 성공적으로 취소하였습니다."),
  SUCCESS_UPDATE_COURSE_STATUS(HttpStatus.OK, "강의 상태를 성공적으로 변경하였습니다."),
  SUCCESS_INQUIRY_COURSE_ENROLLMENTS(HttpStatus.OK, "수강생 목록을 조회했습니다."),
  ;

  private final HttpStatus status;
  private final String message;
}
