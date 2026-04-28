package com.example.assignment.creator.type;

import lombok.Getter;

@Getter
public enum SettlementStatus {
  PENDING("정산 대기"),
  CONFIRMED("정산 확정"),
  PAID("정산 완료");

  private final String value;

  SettlementStatus(String value) {
    this.value = value;
  }
}
