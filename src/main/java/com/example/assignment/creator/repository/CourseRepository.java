package com.example.assignment.creator.repository;

import com.example.assignment.common.entity.User;
import com.example.assignment.creator.entity.Course;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

  Page<Course> findAllByUser(User user, Pageable pageable);

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