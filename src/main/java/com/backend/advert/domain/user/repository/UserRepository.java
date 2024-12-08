package com.backend.advert.domain.user.repository;

import com.backend.advert.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    /**
     * 이메일로 사용자를 조회합니다.
     *
     * @param email 사용자 이메일
     * @return 사용자 엔티티 (Optional)
     */
    Optional<User> findByEmail(String email);

    /**
     * 특정 이메일이 존재하는지 확인합니다.
     *
     * @param email 사용자 이메일
     * @return 존재 여부
     */
    boolean existsByEmail(String email);
}
