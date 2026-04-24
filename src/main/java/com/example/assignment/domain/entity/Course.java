package com.example.assignment.domain.entity;

import com.example.assignment.domain.dto.request.CourseReq;
import com.example.assignment.domain.type.CourseStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Course extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long courseId;

  private String title;

  private String description;

  private Long amount;

  private Long personnel;

  private LocalDate startPeriodAt;

  private LocalDate endPeriodAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Enumerated(EnumType.STRING)
  private CourseStatus courseStatus;

  private Long enrollmentCnt;

  public static Course toEntity(CourseReq courseReq, User user) {
    return Course.builder()
        .title(courseReq.getTitle())
        .description(courseReq.getDescription())
        .amount(courseReq.getAmount())
        .personnel(courseReq.getPersonnel())
        .startPeriodAt(courseReq.getStartPeriodAt())
        .endPeriodAt(courseReq.getEndPeriodAt())
        .user(user)
        .courseStatus(courseReq.getCourseStatus())
        .enrollmentCnt(0L)
        .build();
  }

  public boolean isFull() {
    return enrollmentCnt >= personnel;
  }

  public void increaseEnrollmentCnt() {
    this.enrollmentCnt++;

    // 정원이 꽉 찼으면 자동으로 CLOSED 로 변경
    if (this.isFull()) {
      this.courseStatus = CourseStatus.CLOSED;
    }
  }

  public boolean isNotAvailable() {
    return courseStatus == CourseStatus.DRAFT
        || courseStatus == CourseStatus.CLOSED;
  }

}