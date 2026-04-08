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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {

    @Mock
    private PointHistoryRepository pointHistoryRepository;

    @Mock
    private PointBalanceRepository pointBalanceRepository;

    @InjectMocks
    private PointService pointService;

    // T-POINT-001
    @Test
    @DisplayName("정상 포인트 적립 시 이력 생성 + 잔액 증가")
    void earn_success() {
        // given
        PointRequest request = new PointRequest(1L, 500, "가입 축하 포인트");
        PointBalance balance = new PointBalance(1L, 1L, 0);
        given(pointBalanceRepository.findByUserId(1L)).willReturn(Optional.of(balance));
        given(pointHistoryRepository.save(any(PointHistory.class)))
                .willAnswer(invocation -> {
                    PointHistory h = invocation.getArgument(0);
                    return PointHistory.builder()
                            .id(1L).userId(h.getUserId()).amount(h.getAmount())
                            .type(h.getType()).description(h.getDescription()).build();
                });

        // when
        PointResponse response = pointService.earn(request);

        // then
        assertThat(response.getType()).isEqualTo(PointType.EARN);
        assertThat(response.getAmount()).isEqualTo(500);
        assertThat(response.getBalance()).isEqualTo(500);
    }

    // T-POINT-002
    @Test
    @DisplayName("정상 포인트 차감 시 이력 생성 + 잔액 감소")
    void deduct_success() {
        // given
        PointRequest request = new PointRequest(1L, 100, "게시글 작성");
        PointBalance balance = new PointBalance(1L, 1L, 500);
        given(pointBalanceRepository.findByUserId(1L)).willReturn(Optional.of(balance));
        given(pointHistoryRepository.save(any(PointHistory.class)))
                .willAnswer(invocation -> {
                    PointHistory h = invocation.getArgument(0);
                    return PointHistory.builder()
                            .id(2L).userId(h.getUserId()).amount(h.getAmount())
                            .type(h.getType()).description(h.getDescription()).build();
                });

        // when
        PointResponse response = pointService.deduct(request);

        // then
        assertThat(response.getType()).isEqualTo(PointType.DEDUCT);
        assertThat(response.getAmount()).isEqualTo(100);
        assertThat(response.getBalance()).isEqualTo(400);
    }

    // T-POINT-003
    @Test
    @DisplayName("잔액 부족 시 차감하면 예외 발생")
    void deduct_insufficientBalance_throwsException() {
        // given
        PointRequest request = new PointRequest(1L, 600, "게시글 작성");
        PointBalance balance = new PointBalance(1L, 1L, 500);
        given(pointBalanceRepository.findByUserId(1L)).willReturn(Optional.of(balance));

        // when & then
        assertThatThrownBy(() -> pointService.deduct(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("잔액이 부족합니다");
    }

    // T-POINT-004
    @Test
    @DisplayName("잔액 조회 시 현재 잔액 반환")
    void getBalance_success() {
        // given
        PointBalance balance = new PointBalance(1L, 1L, 410);
        given(pointBalanceRepository.findByUserId(1L)).willReturn(Optional.of(balance));

        // when
        int result = pointService.getBalance(1L);

        // then
        assertThat(result).isEqualTo(410);
    }

    // T-POINT-005
    @Test
    @DisplayName("최초 적립 시 PointBalance가 자동 생성된다")
    void earn_firstTime_createsBalance() {
        // given
        PointRequest request = new PointRequest(1L, 500, "가입 축하 포인트");
        given(pointBalanceRepository.findByUserId(1L)).willReturn(Optional.empty());
        given(pointBalanceRepository.save(any(PointBalance.class)))
                .willReturn(new PointBalance(1L, 1L, 0));
        given(pointHistoryRepository.save(any(PointHistory.class)))
                .willAnswer(invocation -> {
                    PointHistory h = invocation.getArgument(0);
                    return PointHistory.builder()
                            .id(1L).userId(h.getUserId()).amount(h.getAmount())
                            .type(h.getType()).description(h.getDescription()).build();
                });

        // when
        PointResponse response = pointService.earn(request);

        // then
        assertThat(response.getBalance()).isEqualTo(500);
    }
}
