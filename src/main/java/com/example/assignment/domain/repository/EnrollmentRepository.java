package com.example.assignment.domain.repository;

import com.example.assignment.domain.entity.Course;
import com.example.assignment.domain.entity.Enrollment;
import com.example.assignment.domain.entity.User;
import com.example.assignment.domain.type.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

  boolean existsByCourseAndUserAndEnrollmentStatusNot(Course course, User user, EnrollmentStatus status);
}
