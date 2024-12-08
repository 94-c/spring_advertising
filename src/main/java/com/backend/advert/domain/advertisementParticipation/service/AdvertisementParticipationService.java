package com.backend.advert.domain.advertisementParticipation.service;

import com.backend.advert.config.redis.RedisLockService;
import com.backend.advert.domain.advertisement.entity.Advertisement;
import com.backend.advert.domain.advertisement.exception.AdvertisementNotFoundException;
import com.backend.advert.domain.advertisement.exception.InvalidAdvertisementException;
import com.backend.advert.domain.advertisement.repository.AdvertisementRepository;
import com.backend.advert.domain.advertisementParticipation.dto.AdvertisementParticipationRequest;
import com.backend.advert.domain.advertisementParticipation.dto.AdvertisementParticipationResponse;
import com.backend.advert.domain.advertisementParticipation.entity.AdvertisementParticipation;
import com.backend.advert.domain.advertisementParticipation.repository.AdvertisementParticipationRepository;
import com.backend.advert.common.response.ApiResponse;
import com.backend.advert.domain.point.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdvertisementParticipationService {

    private final AdvertisementRepository advertisementRepository;
    private final AdvertisementParticipationRepository participationRepository;
    private final RedisLockService redisLockService;
    private final PointService pointService;

    /**
     * 광고 참여를 처리하는 메소드입니다.
     * - 광고 참여 이력을 저장하고, 참여 가능 횟수를 차감합니다.
     * - 포인트 적립 서버 호출 실패 시 참여는 완료되지만, 포인트 적립 실패 메시지를 반환합니다.
     *
     * @param request 광고 참여 요청 DTO
     * @return 광고 참여 성공 메시지와 함께 `ApiResponse` 객체 반환
     * @throws AdvertisementNotFoundException 광고가 존재하지 않을 경우 발생
     * @throws InvalidAdvertisementException 광고의 참여 가능 횟수가 0 이하일 경우 발생
     */
    @Transactional
    public ApiResponse<String> participateInAdvertisement(AdvertisementParticipationRequest request) {
        String lockKey = "advertisement:" + request.getAdvertisementId();

        try {
            // Redis Lock 획득
            redisLockService.lock(lockKey);

            // 광고 정보 조회
            Advertisement advertisement = advertisementRepository.findById(request.getAdvertisementId())
                    .orElseThrow(() -> new AdvertisementNotFoundException("광고를 찾을 수 없습니다."));

            // 남은 참여 가능 횟수 확인
            validateAdvertisement(advertisement);

            // 참여 이력 검증
            validateParticipationHistory(request.getUserId(), advertisement.getId());

            // 참여 이력 저장
            AdvertisementParticipation participation = request.toEntity(advertisement);
            participationRepository.save(participation);

            // 광고 참여 가능 횟수 차감
            advertisement.decrementParticipationCount();

            // 포인트 적립
            try {
                pointService.addPoints(request.getUserId(), advertisement.getRewardPoints());
            } catch (Exception e) {
                // 포인트 적립 실패 처리
                return ApiResponse.success(HttpStatus.OK, "광고 참여는 완료되었으나 포인트 적립에 실패했습니다.", null);
            }

            return ApiResponse.success(HttpStatus.OK, "광고 참여가 완료되었습니다.", null);

        } finally {
            // Redis Lock 해제
            redisLockService.unlock(lockKey);
        }
    }



    /**
     * 특정 사용자의 광고 참여 이력을 조회합니다.
     * - 조회 기간 내의 참여 이력을 광고 참여 시각 기준으로 오래된 순으로 정렬하여 반환합니다.
     * - 페이지네이션을 적용하여 최대 50개의 이력을 조회합니다.
     *
     * @param userId 사용자 ID
     * @param startDate 조회 시작 날짜 (포함)
     * @param endDate 조회 종료 날짜 (포함)
     * @param page 조회할 페이지 번호
     * @param size 한 번에 조회할 이력 수
     * @return 광고 참여 이력 페이지 응답 DTO
     */
    @Transactional(readOnly = true)
    public ApiResponse<Page<AdvertisementParticipationResponse>> getParticipationHistory(
            UUID userId, LocalDateTime startDate, LocalDateTime endDate, int page, int size) {

        // 페이지 요청 생성
        PageRequest pageable = PageRequest.of(page, Math.min(size, 50), Sort.by("participatedAt").ascending());

        // 광고 참여 이력 조회
        Page<AdvertisementParticipation> participations = participationRepository.findByUserIdAndParticipatedAtBetween(userId, startDate, endDate, pageable);

        // 엔티티 -> DTO 매핑
        Page<AdvertisementParticipationResponse> responsePage =
                participations.map(AdvertisementParticipationResponse::fromEntity);

        return ApiResponse.success(HttpStatus.OK, "광고 참여 이력을 성공적으로 조회했습니다.", responsePage);
    }

    /**
     * 광고의 남은 참여 가능 횟수 검증
     * @param advertisement 광고 객체
     */
    private void validateAdvertisement(Advertisement advertisement) {
        if (advertisement.getRemainingParticipationCount() <= 0) {
            throw new InvalidAdvertisementException("남은 참여 가능 횟수가 없습니다.");
        }
    }

    /**
     * 사용자 참여 이력 검증
     * @param userId 사용자 ID
     * @param advertisementId 광고 ID
     */
    private void validateParticipationHistory(UUID userId, UUID advertisementId) {
        boolean hasParticipated = participationRepository.existsByUserIdAndAdvertisementId(userId, advertisementId);
        if (hasParticipated) {
            throw new InvalidAdvertisementException("이미 참여한 광고입니다.");
        }
    }
}
