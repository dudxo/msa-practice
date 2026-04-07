package com.msa.board.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msa.board.dto.CreateBoardRequest;
import com.msa.board.dto.BoardResponse;
import com.msa.board.service.BoardService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BoardController.class)
class BoardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BoardService boardService;

    // T-BOARD-001 (Controller)
    @Test
    @DisplayName("POST /boards - 정상 요청이면 201 반환")
    void createBoard_success() throws Exception {
        // given
        CreateBoardRequest request = new CreateBoardRequest("제목", "내용", 1L);
        BoardResponse response = new BoardResponse(1L, "제목", "내용", 1L, "홍길동", LocalDateTime.now());
        given(boardService.createBoard(any())).willReturn(response);

        // when & then
        mockMvc.perform(post("/boards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("제목"));
    }

    // T-BOARD-002
    @Test
    @DisplayName("POST /boards - 제목이 빈값이면 400 반환")
    void createBoard_blankTitle_returns400() throws Exception {
        // given
        CreateBoardRequest request = new CreateBoardRequest("", "내용", 1L);

        // when & then
        mockMvc.perform(post("/boards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // T-BOARD-003 (Controller)
    @Test
    @DisplayName("GET /boards/{id} - 존재하는 ID면 200 반환")
    void getBoard_success() throws Exception {
        // given
        BoardResponse response = new BoardResponse(1L, "제목", "내용", 1L, "홍길동", LocalDateTime.now());
        given(boardService.getBoard(1L)).willReturn(response);

        // when & then
        mockMvc.perform(get("/boards/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("제목"));
    }

    // T-BOARD-005 (Controller)
    @Test
    @DisplayName("GET /boards - 전체 조회 시 200 반환")
    void getAllBoards_success() throws Exception {
        // given
        List<BoardResponse> responses = List.of(
                new BoardResponse(1L, "제목1", "내용1", 1L, "홍길동", LocalDateTime.now()),
                new BoardResponse(2L, "제목2", "내용2", 2L, "김철수", LocalDateTime.now())
        );
        given(boardService.getAllBoards()).willReturn(responses);

        // when & then
        mockMvc.perform(get("/boards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}
