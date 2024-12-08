package com.backend.advert.domain.advertisement.repository;

import com.backend.advert.domain.advertisement.entity.Advertisement;
import com.backend.advert.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface AdvertisementRepository extends JpaRepository<Advertisement, UUID> {
    // 광고명으로 조회
    Optional<Advertisement> findByTitle(String title);

    /**
     * 노출 기간 내에 있고, 남은 참여 횟수가 있는 광고를 조회합니다.
     *
     * @param startDate 광고 노출 시작일
     * @param endDate 광고 노출 종료일
     * @param minRemainingCount 최소 남은 참여 횟수
     * @param pageable 페이징 정보
     * @return 광고 목록
     */
    Page<Advertisement> findByExposureStartDateBeforeAndExposureEndDateAfterAndRemainingParticipationCountGreaterThan(
            LocalDateTime startDate, LocalDateTime endDate, int minRemainingCount, Pageable pageable);

    /**
     * 특정 광고에 사용자가 참가한 적이 있는지 확인합니다.
     *
     * @param advertisementId 광고 ID
     * @param user 사용자 엔티티
     * @return 참가 여부
     */
    boolean existsByIdAndParticipantsContains(UUID advertisementId, User user);
}
