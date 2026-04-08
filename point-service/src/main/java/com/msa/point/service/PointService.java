package com.msa.point.service;

import com.msa.point.dto.PointRequest;
import com.msa.point.dto.PointResponse;
import com.msa.point.entity.PointBalance;
import com.msa.point.entity.PointHistory;
import com.msa.point.entity.PointType;
import com.msa.point.repository.PointBalanceRepository;
import com.msa.point.repository.PointHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PointService {

    private final PointHistoryRepository pointHistoryRepository;
    private final PointBalanceRepository pointBalanceRepository;

    @Transactional
    public PointResponse earn(PointRequest request) {
        PointBalance balance = getOrCreateBalance(request.getUserId());
        balance.earn(request.getAmount());

        PointHistory history = PointHistory.builder()
                .userId(request.getUserId())
                .amount(request.getAmount())
                .type(PointType.EARN)
                .description(request.getDescription())
                .build();

        PointHistory saved = pointHistoryRepository.save(history);
        return PointResponse.from(saved, balance.getBalance());
    }

    @Transactional
    public PointResponse deduct(PointRequest request) {
        PointBalance balance = getOrCreateBalance(request.getUserId());
        balance.deduct(request.getAmount());

        PointHistory history = PointHistory.builder()
                .userId(request.getUserId())
                .amount(request.getAmount())
                .type(PointType.DEDUCT)
                .description(request.getDescription())
                .build();

        PointHistory saved = pointHistoryRepository.save(history);
        return PointResponse.from(saved, balance.getBalance());
    }

    @Transactional
    public PointResponse refund(PointRequest request) {
        PointBalance balance = getOrCreateBalance(request.getUserId());
        balance.earn(request.getAmount());

        PointHistory history = PointHistory.builder()
                .userId(request.getUserId())
                .amount(request.getAmount())
                .type(PointType.REFUND)
                .description(request.getDescription())
                .build();

        PointHistory saved = pointHistoryRepository.save(history);
        return PointResponse.from(saved, balance.getBalance());
    }

    public int getBalance(Long userId) {
        return pointBalanceRepository.findByUserId(userId)
                .map(PointBalance::getBalance)
                .orElseThrow(() -> new IllegalArgumentException("잔액 정보를 찾을 수 없습니다. userId=" + userId));
    }

    private PointBalance getOrCreateBalance(Long userId) {
        return pointBalanceRepository.findByUserId(userId)
                .orElseGet(() -> pointBalanceRepository.save(PointBalance.createForUser(userId)));
    }
}
