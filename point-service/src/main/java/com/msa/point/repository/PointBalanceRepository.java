package com.msa.point.repository;

import com.msa.point.entity.PointBalance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PointBalanceRepository extends JpaRepository<PointBalance, Long> {
    Optional<PointBalance> findByUserId(Long userId);
}
