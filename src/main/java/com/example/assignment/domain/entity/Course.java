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

  private Long personnel;

  private LocalDate startPeriodAt;

  private LocalDate endPeriodAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Enumerated(EnumType.STRING)
  private CourseStatus courseStatus;

  public static Course toEntity(CourseReq courseReq, User user) {
    return Course.builder()
        .title(courseReq.getTitle())
        .description(courseReq.getDescription())
        .personnel(courseReq.getPersonnel())
        .startPeriodAt(courseReq.getStartPeriodAt())
        .endPeriodAt(courseReq.getEndPeriodAt())
        .user(user)
        .courseStatus(courseReq.getCourseStatus())
        .build();
  }

}