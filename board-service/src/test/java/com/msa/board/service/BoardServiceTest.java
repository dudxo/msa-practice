package com.msa.board.service;

import com.msa.board.client.UserServiceClient;
import com.msa.board.dto.CreateBoardRequest;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class BoardServiceTest {

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private BoardService boardService;

    // T-BOARD-001
    @Test
    @DisplayName("정상 요청으로 게시글을 작성하면 BoardResponse가 반환된다")
    void createBoard_success() {
        // given
        CreateBoardRequest request = new CreateBoardRequest("제목", "내용", 1L);
        Board savedBoard = Board.builder()
                .id(1L)
                .title("제목")
                .content("내용")
                .authorId(1L)
                .build();
        given(boardRepository.save(any(Board.class))).willReturn(savedBoard);
        given(userServiceClient.getUserName(1L)).willReturn("홍길동");

        // when
        BoardResponse response = boardService.createBoard(request);

        // then
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("제목");
        assertThat(response.getAuthorId()).isEqualTo(1L);
    }

    // T-BOARD-003
    @Test
    @DisplayName("존재하는 ID로 게시글을 조회하면 BoardResponse가 반환된다")
    void getBoard_success() {
        // given
        Board board = Board.builder()
                .id(1L)
                .title("제목")
                .content("내용")
                .authorId(1L)
                .build();
        given(boardRepository.findById(1L)).willReturn(Optional.of(board));
        given(userServiceClient.getUserName(1L)).willReturn("홍길동");

        // when
        BoardResponse response = boardService.getBoard(1L);

        // then
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("제목");
    }

    // T-BOARD-004
    @Test
    @DisplayName("존재하지 않는 ID로 게시글을 조회하면 예외가 발생한다")
    void getBoard_notFound_throwsException() {
        // given
        given(boardRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> boardService.getBoard(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("게시글을 찾을 수 없습니다");
    }

    // T-BOARD-005
    @Test
    @DisplayName("게시글 전체 조회 시 리스트가 반환된다")
    void getAllBoards_success() {
        // given
        Board board1 = Board.builder().id(1L).title("제목1").content("내용1").authorId(1L).build();
        Board board2 = Board.builder().id(2L).title("제목2").content("내용2").authorId(2L).build();
        given(boardRepository.findAll()).willReturn(List.of(board1, board2));
        given(userServiceClient.getUserNamesByIds(anyList()))
                .willReturn(Map.of(1L, "홍길동", 2L, "김철수"));

        // when
        List<BoardResponse> responses = boardService.getAllBoards();

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getTitle()).isEqualTo("제목1");
    }
}
