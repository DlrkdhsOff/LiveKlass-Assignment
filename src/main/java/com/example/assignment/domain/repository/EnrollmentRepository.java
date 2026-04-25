package com.example.assignment.domain.repository;

import com.example.assignment.domain.entity.Course;
import com.example.assignment.domain.entity.Enrollment;
import com.example.assignment.domain.entity.User;
import com.example.assignment.domain.type.EnrollmentStatus;
import java.util.List;
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
}
