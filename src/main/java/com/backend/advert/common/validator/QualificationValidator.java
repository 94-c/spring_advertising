package com.backend.advert.common.validator;

import com.backend.advert.domain.advertisement.dto.QualificationCriteria;
import com.backend.advert.domain.advertisement.repository.AdvertisementRepository;
import com.backend.advert.domain.user.entity.User;
import com.backend.advert.domain.user.exception.UserNotFoundException;
import com.backend.advert.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QualificationValidator {

    private final AdvertisementRepository advertisementRepository;
    private final UserRepository userRepository;

    /**
     * 자격 조건을 검증합니다.
     *
     * @param criteria 자격 조건
     * @param userId 검증 대상 사용자의 ID
     * @return 조건 만족 여부
     * @throws IllegalArgumentException 사용자 정보를 찾을 수 없을 경우 예외 발생
     */
    public boolean isValid(QualificationCriteria criteria, UUID userId) {
        if (criteria == null) {
            return true; // 조건이 없으면 항상 통과
        }

        // userId를 기반으로 User 조회 (존재하지 않으면 예외 발생)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId));

        // 첫 참가 조건 확인
        if (criteria.isFirstTimeParticipation()) {
            if (user.getParticipationCount() > 0) {
                return false; // 첫 참가 조건 만족하지 않음
            }
        }

        // 최소 참가 횟수 조건 확인
        if (criteria.getMinParticipationCount() != null) {
            if (user.getParticipationCount() < criteria.getMinParticipationCount()) {
                return false; // 최소 참가 횟수 조건 만족하지 않음
            }
        }

        // 특정 광고 참가 이력 제외 조건 확인
        if (criteria.getExcludedAdvertisementId() != null) {
            UUID advertisementId = UUID.fromString(criteria.getExcludedAdvertisementId());
            return !advertisementRepository.existsByIdAndParticipantsContains(advertisementId, user); // 특정 광고 참가 이력 조건 만족하지 않음
        }

        return true; // 모든 조건을 만족
    }
}

