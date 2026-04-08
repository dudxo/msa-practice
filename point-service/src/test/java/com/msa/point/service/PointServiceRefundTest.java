package com.msa.point.service;

import com.msa.point.dto.PointRequest;
import com.msa.point.dto.PointResponse;
import com.msa.point.entity.PointBalance;
import com.msa.point.entity.PointHistory;
import com.msa.point.entity.PointType;
import com.msa.point.repository.PointBalanceRepository;
import com.msa.point.repository.PointHistoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class PointServiceRefundTest {

    @Mock
    private PointHistoryRepository pointHistoryRepository;

    @Mock
    private PointBalanceRepository pointBalanceRepository;

    @InjectMocks
    private PointService pointService;

    // T-POINT-006
    @Test
    @DisplayName("정상 환불 시 이력 생성(REFUND) + 잔액 증가")
    void refund_success() {
        // given
        PointRequest request = new PointRequest(1L, 100, "게시글 작성 실패 환불");
        PointBalance balance = new PointBalance(1L, 1L, 400);
        given(pointBalanceRepository.findByUserId(1L)).willReturn(Optional.of(balance));
        given(pointHistoryRepository.save(any(PointHistory.class)))
                .willAnswer(invocation -> {
                    PointHistory h = invocation.getArgument(0);
                    return PointHistory.builder()
                            .id(5L).userId(h.getUserId()).amount(h.getAmount())
                            .type(h.getType()).description(h.getDescription()).build();
                });

        // when
        PointResponse response = pointService.refund(request);

        // then
        assertThat(response.getType()).isEqualTo(PointType.REFUND);
        assertThat(response.getAmount()).isEqualTo(100);
        assertThat(response.getBalance()).isEqualTo(500);
    }
}
