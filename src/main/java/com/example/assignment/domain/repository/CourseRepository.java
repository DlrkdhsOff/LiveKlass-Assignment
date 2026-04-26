package com.example.assignment.domain.repository;

import com.example.assignment.domain.entity.Course;
import com.example.assignment.domain.entity.User;
import com.example.assignment.domain.type.CourseStatus;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

  @Query("""
      SELECT COUNT(c) > 0
      FROM Course c
      WHERE c.user = :user
        AND c.title = :title
        AND c.description = :description
        AND c.amount = :amount
        AND c.personnel = :personnel
        AND c.startPeriodAt = :startPeriodAt
        AND c.endPeriodAt = :endPeriodAt
        AND c.courseStatus = :courseStatus
      """)
  boolean existsDuplicateCourse(
      @Param("user") User user,
      @Param("title") String title,
      @Param("description") String description,
      @Param("amount") Long amount,
      @Param("personnel") Long personnel,
      @Param("startPeriodAt") LocalDate startPeriodAt,
      @Param("endPeriodAt") LocalDate endPeriodAt,
      @Param("courseStatus") CourseStatus courseStatus
  );

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT c FROM Course c WHERE c.courseId = :courseId")
  Optional<Course> findByIdWithLock(@Param("courseId") Long courseId);

  @Query("""
    SELECT c FROM Course c
    WHERE c.courseStatus IN ('OPEN', 'DRAFT')
      AND c.startPeriodAt <= :tomorrow
    """)
  List<Course> findAllOpenOrDraftCoursesBeforeStart(@Param("tomorrow") LocalDate tomorrow);
}