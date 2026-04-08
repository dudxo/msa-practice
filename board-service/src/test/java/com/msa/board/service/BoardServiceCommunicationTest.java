package com.msa.board.service;

import com.msa.board.client.PointServiceClient;
import com.msa.board.client.UserServiceClient;
import com.msa.board.event.BoardEventProducer;
import com.msa.board.dto.BoardResponse;
import com.msa.board.entity.Board;
import com.msa.board.repository.BoardRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class BoardServiceCommunicationTest {

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

    // T-BOARD-006
    @Test
    @DisplayName("게시글 단건 조회 시 authorName이 포함된다")
    void getBoard_includesAuthorName() {
        // given
        Board board = Board.builder().id(1L).title("제목").content("내용").authorId(1L).build();
        given(boardRepository.findById(1L)).willReturn(Optional.of(board));
        given(userServiceClient.getUserName(1L)).willReturn("홍길동");

        // when
        BoardResponse response = boardService.getBoard(1L);

        // then
        assertThat(response.getAuthorName()).isEqualTo("홍길동");
    }

    // T-BOARD-007
    @Test
    @DisplayName("User Service 장애 시 authorName이 '알 수 없음'으로 반환된다")
    void getBoard_userServiceDown_fallback() {
        // given
        Board board = Board.builder().id(1L).title("제목").content("내용").authorId(1L).build();
        given(boardRepository.findById(1L)).willReturn(Optional.of(board));
        given(userServiceClient.getUserName(1L)).willReturn("알 수 없음");

        // when
        BoardResponse response = boardService.getBoard(1L);

        // then
        assertThat(response.getAuthorName()).isEqualTo("알 수 없음");
    }

    // T-BOARD-008
    @Test
    @DisplayName("게시글 전체 조회 시 모든 게시글에 authorName이 포함된다")
    void getAllBoards_includesAuthorNames() {
        // given
        List<Board> boards = List.of(
                Board.builder().id(1L).title("제목1").content("내용1").authorId(1L).build(),
                Board.builder().id(2L).title("제목2").content("내용2").authorId(2L).build()
        );
        given(boardRepository.findAll()).willReturn(boards);
        given(userServiceClient.getUserNamesByIds(anyList()))
                .willReturn(Map.of(1L, "홍길동", 2L, "김철수"));

        // when
        List<BoardResponse> responses = boardService.getAllBoards();

        // then
        assertThat(responses.get(0).getAuthorName()).isEqualTo("홍길동");
        assertThat(responses.get(1).getAuthorName()).isEqualTo("김철수");
    }

    // T-BOARD-009
    @Test
    @DisplayName("전체 조회 중 User Service 장애 시 모든 authorName이 '알 수 없음'이다")
    void getAllBoards_userServiceDown_fallback() {
        // given
        List<Board> boards = List.of(
                Board.builder().id(1L).title("제목1").content("내용1").authorId(1L).build()
        );
        given(boardRepository.findAll()).willReturn(boards);
        given(userServiceClient.getUserNamesByIds(anyList()))
                .willReturn(Map.of());

        // when
        List<BoardResponse> responses = boardService.getAllBoards();

        // then
        assertThat(responses.get(0).getAuthorName()).isEqualTo("알 수 없음");
    }
}
