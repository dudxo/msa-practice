package com.msa.board.service;

import com.msa.board.client.PointServiceClient;
import com.msa.board.client.UserServiceClient;
import com.msa.board.event.BoardEventProducer;
import com.msa.board.dto.BoardResponse;
import com.msa.board.dto.CreateBoardRequest;
import com.msa.board.entity.Board;
import com.msa.board.repository.BoardRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BoardServiceSagaTest {

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private PointServiceClient pointServiceClient;

    @Mock
    private BoardEventProducer boardEventProducer;

    @InjectMocks
    private BoardService boardService;

    // T-BOARD-012
    @Test
    @DisplayName("게시글 생성 실패 시 포인트 환불(보상 트랜잭션)이 호출된다")
    void createBoard_boardSaveFails_refundsCalled() {
        // given
        CreateBoardRequest request = new CreateBoardRequest("제목", "내용", 1L);
        willDoNothing().given(pointServiceClient).deductPoints(1L, 100, "게시글 작성");
        given(boardRepository.save(any(Board.class)))
                .willThrow(new RuntimeException("DB 저장 실패"));

        // when & then
        assertThatThrownBy(() -> boardService.createBoard(request))
                .isInstanceOf(RuntimeException.class);

        verify(pointServiceClient).refundPoints(1L, 100, "게시글 작성 실패 환불");
    }

    // T-BOARD-013
    @Test
    @DisplayName("게시글 생성 실패 + 환불 성공 시에도 예외는 클라이언트에 전파된다")
    void createBoard_boardSaveFails_exceptionPropagated() {
        // given
        CreateBoardRequest request = new CreateBoardRequest("제목", "내용", 1L);
        willDoNothing().given(pointServiceClient).deductPoints(1L, 100, "게시글 작성");
        given(boardRepository.save(any(Board.class)))
                .willThrow(new RuntimeException("DB 저장 실패"));
        willDoNothing().given(pointServiceClient).refundPoints(1L, 100, "게시글 작성 실패 환불");

        // when & then
        assertThatThrownBy(() -> boardService.createBoard(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("DB 저장 실패");
    }

    // T-BOARD-014
    @Test
    @DisplayName("정상 흐름에서는 환불이 호출되지 않는다")
    void createBoard_success_noRefund() {
        // given
        CreateBoardRequest request = new CreateBoardRequest("제목", "내용", 1L);
        Board savedBoard = Board.builder().id(1L).title("제목").content("내용").authorId(1L).build();

        willDoNothing().given(pointServiceClient).deductPoints(1L, 100, "게시글 작성");
        given(boardRepository.save(any(Board.class))).willReturn(savedBoard);
        given(userServiceClient.getUserName(1L)).willReturn("홍길동");

        // when
        BoardResponse response = boardService.createBoard(request);

        // then
        assertThat(response.getTitle()).isEqualTo("제목");
        verify(pointServiceClient, never()).refundPoints(any(), any(int.class), any());
    }
}
