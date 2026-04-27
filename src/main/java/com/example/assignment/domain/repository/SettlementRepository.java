package com.example.assignment.domain.repository;

import com.example.assignment.domain.entity.Settlement;
import com.example.assignment.domain.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, Long> {

  Optional<Settlement> findByUserAndSettlementMonth(User user, String settlementMonth);
}
