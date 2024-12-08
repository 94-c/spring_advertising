package com.backend.advert.domain.user.controller;

import com.backend.advert.common.response.ApiResponse;
import com.backend.advert.domain.user.dto.CreateUserRequest;
import com.backend.advert.domain.user.dto.UserResponse;
import com.backend.advert.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 새로운 사용자를 생성합니다.
     *
     * @return 생성된 사용자 정보를 포함한 성공 메시지
     */
    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@RequestBody @Valid CreateUserRequest userRequest) {
        ApiResponse<UserResponse> response = userService.saveUser(userRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 사용자 ID로 사용자 정보를 조회합니다.
     *
     * @param userId 조회할 사용자 ID
     * @return 사용자 정보를 포함한 성공 메시지
     */
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable UUID userId) {
        ApiResponse<UserResponse> response = userService.getUserById(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 사용자 이메일로 사용자 정보를 조회합니다.
     *
     * @param email 조회할 사용자 이메일
     * @return 사용자 정보를 포함한 성공 메시지
     */
    @GetMapping
    public ResponseEntity<ApiResponse<UserResponse>> getUserByEmail(@RequestParam String email) {
        ApiResponse<UserResponse> response = userService.getUserByEmail(email);
        return ResponseEntity.ok(response);
    }
}
