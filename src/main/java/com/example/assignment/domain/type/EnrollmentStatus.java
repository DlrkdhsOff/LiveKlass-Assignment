package com.example.assignment.domain.type;

import lombok.Getter;

@Getter
public enum EnrollmentStatus {
  PENDING("신청완료, 결제대기"),
  CONFIRMED("결제완료, 수강 확정"),
  CANCELLED("수강 취소"),
  WAITLISTED("대기중");

  private final String value;

  EnrollmentStatus(String value) {
    this.value = value;
  }

}
