package com.example.assignment.student.repository;

import com.example.assignment.common.entity.User;
import com.example.assignment.creator.entity.Course;
import com.example.assignment.student.entity.Enrollment;
import com.example.assignment.student.type.EnrollmentStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

  @Query("""
    SELECT COUNT(e) > 0 FROM Enrollment e
    WHERE e.course = :course
      AND e.user = :user
      AND e.enrollmentStatus != :status
    """)
  boolean existsActiveEnrollment(
      @Param("course") Course course,
      @Param("user") User user,
      @Param("status") EnrollmentStatus status);

  Page<Enrollment> findAllByUser(User user, Pageable pageable);

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
      @Param("status") EnrollmentStatus status);


  List<Enrollment> findAllByCourse_CourseIdIn(List<Long> courseIds);
}
