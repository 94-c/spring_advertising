package com.backend.advert.domain.user.dto;

import com.backend.advert.domain.user.entity.User;
import lombok.Getter;

import java.util.UUID;

@Getter
public class UserResponse {

    private final UUID id;
    private final String email;
    private final String username;
    private final Integer participationCount;

    /**
     * UserResponse 생성자.
     *
     * @param id 사용자 ID
     * @param email 사용자 이메일
     * @param username 사용자 이름
     * @param participationCount 광고 참여 횟수
     */
    private UserResponse(UUID id, String email, String username, Integer participationCount) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.participationCount = participationCount;
    }

    /**
     * User 엔티티로부터 UserResponse 객체를 생성합니다.
     *
     * @param user User 엔티티
     * @return UserResponse 객체
     */
    public static UserResponse fromEntity(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getParticipationCount()
        );
    }
}
