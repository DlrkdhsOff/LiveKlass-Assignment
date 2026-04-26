package com.example.assignment.domain.repository;

import com.example.assignment.domain.entity.Course;
import com.example.assignment.domain.entity.Enrollment;
import com.example.assignment.domain.entity.User;
import com.example.assignment.domain.type.EnrollmentStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

  boolean existsByCourseAndUserAndEnrollmentStatusNot(Course course, User user, EnrollmentStatus status);

  @Query("""
    SELECT e FROM Enrollment e
    JOIN FETCH e.course
    WHERE e.user = :user
    ORDER BY e.createdAt DESC
    """)
  List<Enrollment> findAllByUserWithCourse(@Param("user") User user);

  @Query("""
    SELECT e FROM Enrollment e
    JOIN FETCH e.user
    WHERE e.course = :course
    ORDER BY e.createdAt DESC
    """)
  List<Enrollment> findAllByCourseWithUser(@Param("course") Course course);

  @Query("""
    SELECT e FROM Enrollment e
    WHERE e.course = :course
      AND e.enrollmentStatus = 'WAITLISTED'
    ORDER BY e.createdAt ASC
    LIMIT 1
    """)
  Optional<Enrollment> findFirstWaitlistedByCourse(@Param("course") Course course);

  @Query("""
    SELECT e FROM Enrollment e
    JOIN FETCH e.course
    WHERE e.enrollmentId = :enrollmentId
    """)
  Optional<Enrollment> findByIdWithCourse(@Param("enrollmentId") Long enrollmentId);

  @Query("""
    SELECT e FROM Enrollment e
    JOIN FETCH e.course
    WHERE e.enrollmentStatus = :status
      AND e.course.startPeriodAt <= :threeDaysLater
    """)
  List<Enrollment> findAllBeforeCourseStart(
      @Param("threeDaysLater") LocalDate threeDaysLater,
      @Param("status") EnrollmentStatus status
      );



}
