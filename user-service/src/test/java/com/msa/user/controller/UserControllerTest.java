package com.msa.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msa.user.dto.CreateUserRequest;
import com.msa.user.dto.UserResponse;
import com.msa.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    // T-USER-001 (Controller)
    @Test
    @DisplayName("POST /users - 정상 요청이면 201 반환")
    void createUser_success() throws Exception {
        // given
        CreateUserRequest request = new CreateUserRequest("홍길동", "hong@test.com", "password123");
        UserResponse response = new UserResponse(1L, "홍길동", "hong@test.com", LocalDateTime.now());
        given(userService.createUser(any())).willReturn(response);

        // when & then
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("홍길동"));
    }

    // T-USER-005
    @Test
    @DisplayName("POST /users - 이름이 빈값이면 400 반환")
    void createUser_blankName_returns400() throws Exception {
        // given
        CreateUserRequest request = new CreateUserRequest("", "hong@test.com", "password123");

        // when & then
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // T-USER-006
    @Test
    @DisplayName("POST /users - 이메일 형식이 잘못되면 400 반환")
    void createUser_invalidEmail_returns400() throws Exception {
        // given
        CreateUserRequest request = new CreateUserRequest("홍길동", "invalid-email", "password123");

        // when & then
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // T-USER-003 (Controller)
    @Test
    @DisplayName("GET /users/{id} - 존재하는 ID면 200 반환")
    void getUser_success() throws Exception {
        // given
        UserResponse response = new UserResponse(1L, "홍길동", "hong@test.com", LocalDateTime.now());
        given(userService.getUser(1L)).willReturn(response);

        // when & then
        mockMvc.perform(get("/users/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("홍길동"));
    }
}
