package com.example.assignment.domain.repository.querydsl;

import com.example.assignment.domain.entity.Course;
import com.example.assignment.domain.entity.QCourse;
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

  public List<Course> searchCourses(String creatorName, String title, Long minAmount, Long maxAmount,
      LocalDate startPeriodAt, LocalDate endPeriodAt, CourseStatus courseStatus) {
    QCourse course = QCourse.course;

    return queryFactory
        .selectFrom(course)
        .leftJoin(course.user).fetchJoin()
        .where(
            creatorNameEq(creatorName),
            titleContains(title),
            amountGoe(minAmount),
            amountLoe(maxAmount),
            startPeriodAtGoe(startPeriodAt),
            endPeriodAtLoe(endPeriodAt),
            courseStatusEq(courseStatus)
        )
        .fetch();
  }

  // null이면 조건 자체를 제외 (동적 쿼리 핵심)
  private BooleanExpression creatorNameEq(String name) {
    return name == null ? null : QCourse.course.user.name.eq(name)
        .and(QCourse.course.user.userRole.eq(UserRole.CREATORS));
  }

  private BooleanExpression titleContains(String title) {
    return title == null ? null : QCourse.course.title.contains(title);
  }

  private BooleanExpression amountGoe(Long minAmount) {
    return minAmount == null ? null : QCourse.course.amount.goe(minAmount);
  }

  private BooleanExpression amountLoe(Long maxAmount) {
    return maxAmount == null ? null : QCourse.course.amount.loe(maxAmount);
  }

  private BooleanExpression startPeriodAtGoe(LocalDate date) {
    return date == null ? null : QCourse.course.startPeriodAt.goe(date);
  }

  private BooleanExpression endPeriodAtLoe(LocalDate date) {
    return date == null ? null : QCourse.course.endPeriodAt.loe(date);
  }

  private BooleanExpression courseStatusEq(CourseStatus status) {
    return status == null ? null : QCourse.course.courseStatus.eq(status);
  }

}
