package com.example.assignment.creator.repository;

import com.example.assignment.common.entity.User;
import com.example.assignment.creator.entity.Settlement;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, Long> {

  Optional<Settlement> findByUserAndSettlementMonth(User user, String settlementMonth);
}
