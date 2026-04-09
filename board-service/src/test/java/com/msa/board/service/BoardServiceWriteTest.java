package com.msa.board.service;

import com.msa.board.client.PointServiceClient;
import com.msa.board.client.UserServiceClient;
import com.msa.board.event.BoardEventProducer;
import com.msa.board.dto.BoardResponse;
import com.msa.board.dto.CreateBoardRequest;
import com.msa.board.entity.Board;
import com.msa.board.repository.BoardRepository;
import com.msa.board.repository.UserInfoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BoardServiceWriteTest {

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private PointServiceClient pointServiceClient;

    @Mock
    private BoardEventProducer boardEventProducer;

    @Mock
    private UserInfoRepository userInfoRepository;

    @InjectMocks
    private BoardService boardService;

    // T-BOARD-010 (Kafka 전환 후)
    @Test
    @DisplayName("게시글 작성 시 포인트 차감 → 게시글 생성 → Kafka 이벤트 발행 순서로 실행된다")
    void createBoard_withPoints_correctOrder() {
        // given
        CreateBoardRequest request = new CreateBoardRequest("제목", "내용", 1L);
        Board savedBoard = Board.builder().id(1L).title("제목").content("내용").authorId(1L).build();

        willDoNothing().given(pointServiceClient).deductPoints(1L, 100, "게시글 작성");
        given(boardRepository.save(any(Board.class))).willReturn(savedBoard);
        given(userInfoRepository.findByUserId(1L)).willReturn(Optional.empty());
        given(userServiceClient.getUserName(1L)).willReturn("홍길동");

        // when
        BoardResponse response = boardService.createBoard(request);

        // then
        assertThat(response.getTitle()).isEqualTo("제목");

        InOrder order = inOrder(pointServiceClient, boardRepository, boardEventProducer);
        order.verify(pointServiceClient).deductPoints(1L, 100, "게시글 작성");
        order.verify(boardRepository).save(any(Board.class));
        order.verify(boardEventProducer).publishBoardCreated(any());
    }

    // T-BOARD-011
    @Test
    @DisplayName("포인트 부족 시 게시글 작성 실패하고 게시글이 생성되지 않는다")
    void createBoard_insufficientPoints_fails() {
        // given
        CreateBoardRequest request = new CreateBoardRequest("제목", "내용", 1L);
        willThrow(new IllegalStateException("잔액이 부족합니다"))
                .given(pointServiceClient).deductPoints(1L, 100, "게시글 작성");

        // when & then
        assertThatThrownBy(() -> boardService.createBoard(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("잔액이 부족합니다");

        verify(boardRepository, never()).save(any());
    }
}
