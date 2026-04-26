package com.example.assignment.domain.repository.querydsl;

import com.example.assignment.domain.dto.request.CourseReq;
import com.example.assignment.domain.dto.request.CourseSearchReq;
import com.example.assignment.domain.entity.Course;
import com.example.assignment.domain.entity.QCourse;
import com.example.assignment.domain.entity.User;
import com.example.assignment.domain.type.CourseStatus;
import com.example.assignment.domain.type.UserRole;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CourseQueryRepository {

  private final JPAQueryFactory queryFactory;


  /**
   * 강의 목록 동적 조회
   * 조건이 null 이면 해당 조건은 자동으로 제외됨
   */
  public List<Course> searchCourses(CourseSearchReq searchReq) {
    QCourse course = QCourse.course;

    return queryFactory
        .selectFrom(course)
        .leftJoin(course.user).fetchJoin()
        .where(
            creatorNameEq(course, searchReq.getCreatorName()),
            titleContains(course, searchReq.getTitle()),
            amountGoe(course, searchReq.getMinAmount()),
            amountLoe(course, searchReq.getMaxAmount()),
            startPeriodAtGoe(course, searchReq.getStartPeriodAt()),
            endPeriodAtLoe(course, searchReq.getEndPeriodAt()),
            courseStatusEq(course, searchReq.getCourseStatus())
        )
        .fetch();
  }

  /**
   * 강의 중복 여부 확인
   * 동일 강사가 제목, 설명, 가격, 정원, 기간, 상태가 모두 일치하는 강의를 등록하는 경우 중복으로 판단
   * 존재 여부만 확인하므로 LIMIT 1 로 처리 (selectOne + fetchFirst)
   */
  public boolean existsDuplicateCourse(User user, CourseReq courseReq) {
    QCourse course = QCourse.course;

    return queryFactory
        .selectOne()
        .from(course)
        .where(
            course.user.eq(user),
            course.title.eq(courseReq.getTitle()),
            course.description.eq(courseReq.getDescription()),
            course.amount.eq(courseReq.getAmount()),
            course.personnel.eq(courseReq.getPersonnel()),
            course.startPeriodAt.eq(courseReq.getStartPeriodAt()),
            course.endPeriodAt.eq(courseReq.getEndPeriodAt()),
            course.courseStatus.eq(courseReq.getCourseStatus())
        )
        .fetchFirst() != null;
  }

  // null 이면 조건 자체를 제외 (동적 쿼리 핵심)

  private BooleanExpression creatorNameEq(QCourse course, String name) {
    return (name == null || name.isBlank()) ? null
        : course.user.name.eq(name).and(course.user.userRole.eq(UserRole.CREATORS));
  }

  private BooleanExpression titleContains(QCourse course, String title) {
    return (title == null || title.isBlank()) ? null : course.title.contains(title);
  }

  private BooleanExpression amountGoe(QCourse course, Long minAmount) {
    return minAmount == null ? null : course.amount.goe(minAmount);
  }

  private BooleanExpression amountLoe(QCourse course, Long maxAmount) {
    return maxAmount == null ? null : course.amount.loe(maxAmount);
  }

  private BooleanExpression startPeriodAtGoe(QCourse course, LocalDate date) {
    return date == null ? null : course.startPeriodAt.goe(date);
  }

  private BooleanExpression endPeriodAtLoe(QCourse course, LocalDate date) {
    return date == null ? null : course.endPeriodAt.loe(date);
  }

  private BooleanExpression courseStatusEq(QCourse course, CourseStatus status) {
    return status == null ? null : course.courseStatus.eq(status);
  }
}