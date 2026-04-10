package com.msa.user.service;

import com.msa.user.client.PointServiceClient;
import com.msa.user.dto.CreateUserRequest;
import com.msa.user.dto.LoginRequest;
import com.msa.user.dto.LoginResponse;
import com.msa.user.dto.UserResponse;
import com.msa.user.entity.User;
import com.msa.user.event.UserEventProducer;
import com.msa.user.repository.UserRepository;
import com.msa.user.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PointServiceClient pointServiceClient;
    private final UserEventProducer userEventProducer;
    private final JwtUtil jwtUtil;

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalStateException("이미 등록된 이메일입니다.");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(request.getPassword())
                .build();

        User savedUser = userRepository.save(user);
        pointServiceClient.earnPoints(savedUser.getId(), 500, "가입 축하 포인트");
        userEventProducer.publishUserCreated(savedUser.getId(), savedUser.getName(), savedUser.getEmail());
        return UserResponse.from(savedUser);
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다."));

        if (!user.getPassword().equals(request.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        String token = jwtUtil.generateToken(user.getId());
        return new LoginResponse(token, user.getId(), user.getName());
    }

    public UserResponse getUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. id=" + id));
        return UserResponse.from(user);
    }

    public List<UserResponse> getUsersByIds(List<Long> ids) {
        return userRepository.findAllById(ids).stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());
    }
}
