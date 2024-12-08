package com.backend.advert.domain.user.service;

import com.backend.advert.common.response.ApiResponse;
import com.backend.advert.domain.user.dto.CreateUserRequest;
import com.backend.advert.domain.user.dto.UserResponse;
import com.backend.advert.domain.user.entity.User;
import com.backend.advert.domain.user.exception.UserAlreadyExistsException;
import com.backend.advert.domain.user.exception.UserNotFoundException;
import com.backend.advert.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * 새로운 사용자를 저장합니다.
     *
     * @param userRequest 사용자 생성 요청 DTO
     * @return 생성된 사용자 응답 DTO
     */
    public ApiResponse<UserResponse> saveUser(CreateUserRequest userRequest) {
        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new UserAlreadyExistsException("이미 존재하는 이메일입니다: " + userRequest.getEmail());
        }

        User user = userRequest.toEntity();
        User savedUser = userRepository.save(user);

        return ApiResponse.success(HttpStatus.CREATED, "사용자가 성공적으로 생성되었습니다.", UserResponse.fromEntity(savedUser)
        );
    }

    /**
     * 사용자 ID로 사용자 정보를 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 사용자 응답 DTO
     */
    public ApiResponse<UserResponse> getUserById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId));

        return ApiResponse.success(HttpStatus.OK, "사용자 정보를 성공적으로 조회했습니다.", UserResponse.fromEntity(user)
        );
    }

    /**
     * 사용자 이메일로 사용자 정보를 조회합니다.
     *
     * @param email 사용자 이메일
     * @return 사용자 응답 DTO
     */
    public ApiResponse<UserResponse> getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. 이메일: " + email));

        return ApiResponse.success(HttpStatus.OK, "사용자 정보를 성공적으로 조회했습니다.", UserResponse.fromEntity(user)
        );
    }

}
