package com.example.assignment.domain.repository;

import com.example.assignment.domain.entity.CancelRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CancelRecordRepository extends JpaRepository<CancelRecord, Long> {

}
