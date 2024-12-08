package com.backend.advert.domain.user.dto;

import com.backend.advert.domain.user.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CreateUserRequest {

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "유효한 이메일 주소여야 합니다.")
    private String email;

    @NotBlank(message = "사용자 이름은 필수입니다.")
    private String username;

    public CreateUserRequest(String email, String username) {
        this.email = email;
        this.username = username;
    }

    /**
     * CreateUserRequest를 User 엔티티로 변환합니다.
     *
     * @return User 엔티티
     */
    public User toEntity() {
        return User.builder()
                .email(email)
                .username(username)
                .build();
    }
}
