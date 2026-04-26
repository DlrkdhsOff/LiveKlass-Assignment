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

  // =================== 정적 팩토리 메서드 ===================

  /**
   * Course 생성
   * 강의 등록 시 enrollmentCnt 를 0 으로 초기화
   */
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

  // =================== 상태 판별 ===================

  /**
   * 수강 신청 불가 상태 여부 확인
   * DRAFT 상태는 신청 불가
   * CLOSED 는 정원 초과 대기 신청이 가능하므로 제외
   */
  public boolean isNotAvailable() {
    return courseStatus == CourseStatus.DRAFT;
  }

  /**
   * 모집 중(OPEN) 상태 여부 확인
   * 강의 상태 변경 시 중복 전환 방지에 사용
   */
  public boolean isOpen() {
    return courseStatus == CourseStatus.OPEN;
  }

  /**
   * 모집 마감(CLOSED) 상태 여부 확인
   * 강의 상태 변경 및 정원 복구 시 사용
   */
  public boolean isClosed() {
    return courseStatus == CourseStatus.CLOSED;
  }

  /**
   * 본인 강의 여부 확인
   * 강의 상태 변경 및 수강생 목록 조회 시 타인 강의 접근 방지에 사용
   */
  public boolean isNotOwnedBy(Long userId) {
    return !this.user.getUserId().equals(userId);
  }

  // =================== 기간 판별 ===================

  /**
   * 강의 시작 여부 확인
   * 시작일이 지난 강의는 수강 신청 및 대기 신청 불가
   */
  public boolean isStarted() {
    return this.startPeriodAt.isBefore(LocalDate.now());
  }

  /**
   * 강의 기간 만료 여부 확인
   * 종료일이 지난 강의는 상태 변경 불가
   */
  public boolean isExpired() {
    return this.endPeriodAt.isBefore(LocalDate.now());
  }

  // =================== 정원 관리 ===================

  /**
   * 정원 마감 여부 확인
   * 신청 인원(enrollmentCnt) 이 최대 정원(personnel) 에 도달하면 true
   * 수강 신청 시 대기열 등록 여부 판단에 사용
   */
  public boolean isFull() {
    return enrollmentCnt >= personnel;
  }

  /**
   * 수강 인원 증가
   * 수강 신청 시 호출
   * 정원이 꽉 차면 자동으로 CLOSED 상태로 전환
   */
  public void increaseEnrollmentCnt() {
    this.enrollmentCnt++;
    if (this.isFull()) {
      this.courseStatus = CourseStatus.CLOSED;
    }
  }

  /**
   * 수강 인원 감소
   * 수강 취소 시 호출
   * CLOSED 상태에서 자리가 생기면 자동으로 OPEN 으로 복귀
   * 단, 강의 시작 하루 전인 경우에는 OPEN 으로 복귀하지 않음
   */
  public void decreaseEnrollmentCnt() {
    if (this.enrollmentCnt > 0) {
      this.enrollmentCnt--;
    }
    if (this.courseStatus == CourseStatus.CLOSED
        && !this.isFull()
        && this.startPeriodAt.isAfter(LocalDate.now().plusDays(1))) {
      this.courseStatus = CourseStatus.OPEN;
    }
  }

  // =================== 상태 변경 ===================

  /**
   * 강의 상태를 모집 중(OPEN) 으로 변경
   * DRAFT 또는 CLOSED 상태에서 호출
   * 호출 전 상태 유효성 검증은 서비스 레이어에서 담당
   */
  public void openCourse() {
    this.courseStatus = CourseStatus.OPEN;
  }

  /**
   * 강의 상태를 모집 마감(CLOSED) 으로 변경
   * 강사의 수동 마감 또는 정원 초과 시 자동 마감에 사용
   * 호출 전 상태 유효성 검증은 서비스 레이어에서 담당
   */
  public void closeCourse() {
    this.courseStatus = CourseStatus.CLOSED;
  }
}