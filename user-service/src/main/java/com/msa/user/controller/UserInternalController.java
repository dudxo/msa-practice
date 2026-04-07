package com.msa.user.controller;

import com.msa.user.dto.UserResponse;
import com.msa.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class UserInternalController {

    private final UserService userService;

    @PostMapping("/by-ids")
    public List<UserResponse> getUsersByIds(@RequestBody List<Long> ids) {
        return userService.getUsersByIds(ids);
    }
}
