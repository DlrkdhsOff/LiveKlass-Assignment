package com.example.assignment.domain.entity;

import com.example.assignment.domain.type.EnrollmentStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Enrollment extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long enrollmentId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "course_id", nullable = false)
  private Course course;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_Id", nullable = false)
  private User user;

  @Enumerated(EnumType.STRING)
  private EnrollmentStatus enrollmentStatus;

  public static Enrollment toEntity(Course course, User user) {
    return Enrollment.builder()
        .course(course)
        .user(user)
        .enrollmentStatus(EnrollmentStatus.PENDING)
        .build();
  }

  public boolean isConfirmed() {
    return enrollmentStatus == EnrollmentStatus.CONFIRMED;
  }

  public boolean isCancelled() {
    return enrollmentStatus == EnrollmentStatus.CANCELLED;
  }

  public void confirm() {
    this.enrollmentStatus = EnrollmentStatus.CONFIRMED;
  }
}