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
import java.time.LocalDate;
import java.time.LocalDateTime;
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

  // =================== 정적 팩토리 메서드 ===================

  /**
   * Enrollment 생성
   * PENDING  → 일반 수강 신청
   * WAITLISTED → 정원 초과로 인한 대기 신청
   */
  public static Enrollment toEntity(Course course, User user, EnrollmentStatus status) {
    return Enrollment.builder()
        .course(course)
        .user(user)
        .enrollmentStatus(status)
        .build();
  }

  // =================== 상태 판별 ===================

  /**
   * 본인 수강 신청 여부 확인
   * 결제, 취소 시 타인의 수강 신청에 접근하는 것을 방지
   */
  public boolean isNotOwnedBy(Long userId) {
    return !this.user.getUserId().equals(userId);
  }

  /**
   * 결제 완료(CONFIRMED) 상태 여부 확인
   * 중복 결제 방지에 사용
   */
  public boolean isConfirmed() {
    return enrollmentStatus == EnrollmentStatus.CONFIRMED;
  }

  /**
   * 취소(CANCELLED) 상태 여부 확인
   * 재취소 및 취소된 건의 결제 방지에 사용
   */
  public boolean isCancelled() {
    return enrollmentStatus == EnrollmentStatus.CANCELLED;
  }

  /**
   * 대기(WAITLISTED) 상태 여부 확인
   * 대기 중 취소 시 정원 복구 생략 여부 판단에 사용
   */
  public boolean isWaitlisted() {
    return enrollmentStatus == EnrollmentStatus.WAITLISTED;
  }

  /**
   * 취소 가능 여부 확인
   * - PENDING, WAITLISTED: 결제 전이므로 언제든 취소 가능
   * - CONFIRMED: 아래 두 조건을 모두 만족하는 경우에만 취소 가능
   *   1. 결제 완료 시점(updatedAt) 기준 7일 이내
   *   2. 강의 시작 3일 전까지
   * - CANCELLED: 취소 불가 (false 반환)
   */
  public boolean isCancellable() {
    if (this.enrollmentStatus == EnrollmentStatus.PENDING
        || this.enrollmentStatus == EnrollmentStatus.WAITLISTED) {
      return true;
    }
    if (this.enrollmentStatus == EnrollmentStatus.CONFIRMED) {
      boolean withinDeadline = this.getUpdatedAt()
          .plusDays(7)
          .isAfter(LocalDateTime.now());

      boolean beforeCourseStart = this.course.getStartPeriodAt()
          .minusDays(3)
          .isAfter(LocalDate.now());

      return withinDeadline && beforeCourseStart;
    }
    return false;
  }

  // =================== 상태 변경 ===================

  /**
   * 결제 확정
   * PENDING → CONFIRMED 상태 전환
   * 호출 시점에 JPA Auditing 이 updatedAt 을 갱신하며,
   * 이 시점이 취소 가능 기간(7일)의 기준이 됨
   */
  public void confirm() {
    this.enrollmentStatus = EnrollmentStatus.CONFIRMED;
  }

  /**
   * 수강 취소
   * PENDING, CONFIRMED, WAITLISTED → CANCELLED 상태 전환
   */
  public void cancel() {
    this.enrollmentStatus = EnrollmentStatus.CANCELLED;
  }

  /**
   * 대기자 승격
   * WAITLISTED → PENDING 상태 전환
   * 다른 수강생의 취소로 자리가 생겼을 때 호출됨
   */
  public void promote() {
    this.enrollmentStatus = EnrollmentStatus.PENDING;
  }
}