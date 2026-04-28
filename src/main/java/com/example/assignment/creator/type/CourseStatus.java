package com.example.assignment.creator.type;

import lombok.Getter;

@Getter
public enum CourseStatus {
  DRAFT("초안"), OPEN("모집중"), CLOSED("모집 마감");

  private final String value;

  CourseStatus(String value) {
    this.value = value;
  }
}
