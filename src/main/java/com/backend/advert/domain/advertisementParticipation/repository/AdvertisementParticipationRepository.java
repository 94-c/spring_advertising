package com.backend.advert.domain.advertisementParticipation.repository;

import com.backend.advert.domain.advertisementParticipation.entity.AdvertisementParticipation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface AdvertisementParticipationRepository extends JpaRepository<AdvertisementParticipation, UUID> {

    /**
     * 특정 사용자의 광고 참여 이력을 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 해당 사용자의 광고 참여 내역 리스트
     */
    List<AdvertisementParticipation> findByUserId(UUID userId);

    /**
     * 특정 광고에 대한 모든 참여 내역을 조회합니다.
     *
     * @param advertisementId 광고 ID
     * @return 해당 광고의 참여 내역 리스트
     */
    List<AdvertisementParticipation> findByAdvertisementId(UUID advertisementId);

    /**
     * 특정 사용자가 특정 광고에 이미 참여했는지 확인합니다.
     *
     * @param userId 사용자 ID
     * @param advertisementId 광고 ID
     * @return 이미 참여한 경우 true, 그렇지 않으면 false
     */
    boolean existsByUserIdAndAdvertisementId(UUID userId, UUID advertisementId);

    /**
     * 특정 사용자의 기간 내 광고 참여 이력을 조회합니다.
     *
     * @param userId 사용자 ID
     * @param startDate 조회 시작 날짜
     * @param endDate 조회 종료 날짜
     * @param pageable 페이지네이션 정보
     * @return 광고 참여 이력 페이지
     */
    Page<AdvertisementParticipation> findByUserIdAndParticipatedAtBetween(UUID userId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
}
