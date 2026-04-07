package com.msa.user.service;

import com.msa.user.dto.CreateUserRequest;
import com.msa.user.dto.UserResponse;
import com.msa.user.entity.User;
import com.msa.user.repository.UserRepository;
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

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalStateException("이미 등록된 이메일입니다.");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .build();

        User savedUser = userRepository.save(user);
        return UserResponse.from(savedUser);
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
