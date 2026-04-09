package com.msa.board.service;

import com.msa.board.client.PointServiceClient;
import com.msa.board.client.UserServiceClient;
import com.msa.board.dto.BoardResponse;
import com.msa.board.entity.Board;
import com.msa.board.entity.UserInfo;
import com.msa.board.event.BoardEventProducer;
import com.msa.board.repository.BoardRepository;
import com.msa.board.repository.UserInfoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BoardServiceLocalCacheTest {

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

    @Test
    @DisplayName("게시글 단건 조회 시 로컬 UserInfo에서 작성자 이름을 가져온다")
    void getBoard_usesLocalUserInfo() {
        // given
        Board board = Board.builder()
                .id(1L).title("제목").content("내용").authorId(1L)
                .build();
        UserInfo userInfo = UserInfo.builder()
                .userId(1L).name("홍길동").email("hong@test.com")
                .build();

        given(boardRepository.findById(1L)).willReturn(Optional.of(board));
        given(userInfoRepository.findByUserId(1L)).willReturn(Optional.of(userInfo));

        // when
        BoardResponse response = boardService.getBoard(1L);

        // then — 로컬 캐시 사용, REST 호출 없음
        assertThat(response.getAuthorName()).isEqualTo("홍길동");
        verify(userServiceClient, never()).getUserName(1L);
    }

    @Test
    @DisplayName("로컬에 UserInfo가 없으면 REST Fallback으로 가져온다")
    void getBoard_fallsBackToRest_whenNoLocalCache() {
        // given
        Board board = Board.builder()
                .id(1L).title("제목").content("내용").authorId(1L)
                .build();

        given(boardRepository.findById(1L)).willReturn(Optional.of(board));
        given(userInfoRepository.findByUserId(1L)).willReturn(Optional.empty());
        given(userServiceClient.getUserName(1L)).willReturn("REST홍길동");

        // when
        BoardResponse response = boardService.getBoard(1L);

        // then — REST Fallback 사용
        assertThat(response.getAuthorName()).isEqualTo("REST홍길동");
        verify(userServiceClient).getUserName(1L);
    }

    @Test
    @DisplayName("게시글 목록 조회 시 로컬 UserInfo를 우선 사용한다")
    void getAllBoards_usesLocalUserInfoFirst() {
        // given
        Board board1 = Board.builder().id(1L).title("제목1").content("내용1").authorId(1L).build();
        Board board2 = Board.builder().id(2L).title("제목2").content("내용2").authorId(2L).build();

        UserInfo userInfo1 = UserInfo.builder().userId(1L).name("유저1").email("u1@test.com").build();
        // userId=2는 로컬에 없음

        given(boardRepository.findAll()).willReturn(List.of(board1, board2));
        given(userInfoRepository.findAllByUserIdIn(List.of(1L, 2L)))
                .willReturn(List.of(userInfo1));
        given(userServiceClient.getUserName(2L)).willReturn("REST유저2");

        // when
        List<BoardResponse> responses = boardService.getAllBoards();

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getAuthorName()).isEqualTo("유저1");   // 로컬
        assertThat(responses.get(1).getAuthorName()).isEqualTo("REST유저2"); // REST Fallback

        // Batch API는 호출하지 않음 (개별 Fallback)
        verify(userServiceClient, never()).getUserNamesByIds(List.of(1L, 2L));
    }
}
