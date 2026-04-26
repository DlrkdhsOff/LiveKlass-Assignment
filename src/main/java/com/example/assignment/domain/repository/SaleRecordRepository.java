package com.example.assignment.domain.repository;

import com.example.assignment.domain.entity.SaleRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SaleRecordRepository extends JpaRepository<SaleRecord, Long> {

}
