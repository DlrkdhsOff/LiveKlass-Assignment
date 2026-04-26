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

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT c FROM Course c WHERE c.courseId = :courseId")
  Optional<Course> findByIdWithLock(@Param("courseId") Long courseId);

  @Query("""
    SELECT c FROM Course c
    WHERE c.courseStatus IN ('OPEN', 'DRAFT')
      AND c.startPeriodAt <= :threeDaysLater
    """)
  List<Course> findAllOpenOrDraftCoursesBeforeStart(@Param("threeDaysLater") LocalDate threeDaysLater);
}