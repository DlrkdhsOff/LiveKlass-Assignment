package com.example.assignment.creator.repository;

import com.example.assignment.creator.entity.SaleRecord;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SaleRecordRepository extends JpaRepository<SaleRecord, Long> {

  Optional<SaleRecord> findByEnrollment_EnrollmentId(Long enrollmentId);

}
