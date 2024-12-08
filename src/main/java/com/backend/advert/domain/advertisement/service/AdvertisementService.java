package com.backend.advert.domain.advertisement.service;

import com.backend.advert.domain.advertisement.dto.CreateAdvertisementRequest;
import com.backend.advert.domain.advertisement.dto.AdvertisementResponse;
import com.backend.advert.domain.advertisement.dto.QualificationCriteria;
import com.backend.advert.domain.advertisement.entity.Advertisement;
import com.backend.advert.domain.advertisement.exception.AdvertisementAlreadyExistsException;
import com.backend.advert.domain.advertisement.exception.AdvertisementNotFoundException;
import com.backend.advert.domain.advertisement.exception.InvalidAdvertisementException;
import com.backend.advert.domain.advertisement.exception.InvalidQualificationException;
import com.backend.advert.domain.advertisement.repository.AdvertisementRepository;
import com.backend.advert.common.response.ApiResponse;
import com.backend.advert.common.validator.QualificationValidator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdvertisementService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final AdvertisementRepository advertisementRepository;
    private final QualificationValidator qualificationValidator;

    /**
     * 새로운 광고를 저장합니다.
     *
     * 1. 동일한 제목의 광고가 이미 존재하는지 Redis 캐시로 확인합니다.
     * 2. 광고 노출 기간 및 입력 데이터를 검증합니다.
     * 3. 광고를 저장하고 Redis 캐시에 동기화합니다.
     * 4. 성공적으로 저장된 광고를 응답 DTO로 변환하여 반환합니다.
     *
     * @param request 광고 생성 요청 DTO
     * @return 저장된 광고의 응답 DTO
     * @throws AdvertisementAlreadyExistsException 동일한 제목의 광고가 이미 존재할 경우 예외 발생
     */
    @Transactional
    public ApiResponse<AdvertisementResponse> saveAdvertisement(CreateAdvertisementRequest request) {
        // Redis와 DB를 통해 제목 중복 확인
        if (isAdvertisementTitleExists(request.getTitle())) {
            throw new AdvertisementAlreadyExistsException("동일한 이름의 광고가 이미 존재합니다.");
        }

        // 요청 데이터를 기반으로 광고 엔티티 생성
        Advertisement advertisement = request.toEntity();

        // 광고 데이터 검증
        validateAdvertisement(advertisement);

        // 광고 저장
        Advertisement savedAdvertisement = advertisementRepository.save(advertisement);

        // Redis 캐시에 저장
        cacheAdvertisement(savedAdvertisement);

        // 저장된 광고 정보를 응답 DTO로 변환 및 반환
        return ApiResponse.success(HttpStatus.CREATED, "광고가 성공적으로 저장되었습니다.", AdvertisementResponse.fromEntity(savedAdvertisement));
    }

    /**
     * 활성 상태의 광고 목록을 조회합니다.
     * - 광고 참여 가능 횟수가 소진되지 않은 광고
     * - 노출 기간 내에 있는 광고
     * - 광고 참여 시 적립액수가 높은 순으로 조회 (최대 10개)
     *
     * @param page 조회할 페이지 번호
     * @param size 한 번에 조회할 광고의 수
     * @return 활성 상태의 광고 목록 응답 DTO
     */
    @Transactional(readOnly = true)
    @CachePut(value = "activeAdvertisements", key = "'activeAdvertisements_' + #page + '_' + #size")
    public ApiResponse<Page<AdvertisementResponse>> getActiveAdvertisements(int page, int size) {
        LocalDateTime now = LocalDateTime.now();
        Pageable pageable = PageRequest.of(page, size);

        // 광고 목록을 페이지네이션 처리하여 조회
        Page<Advertisement> advertisementPage = advertisementRepository
                .findByExposureStartDateBeforeAndExposureEndDateAfterAndRemainingParticipationCountGreaterThan(
                        now, now, 0, pageable); // 노출 기간과 남은 참여 횟수가 있는 광고만 필터링

        // Advertisement 엔티티를 AdvertisementResponse DTO로 변환 후, 적립액수가 높은 순으로 정렬
        List<AdvertisementResponse> sortedAdvertisements = advertisementPage
                .getContent()
                .stream()
                .map(AdvertisementResponse::fromEntity)
                .sorted(Comparator.comparingInt(AdvertisementResponse::getRewardPoints).reversed())
                .collect(Collectors.toList());

        Page<AdvertisementResponse> sortedPage = new PageImpl<>(sortedAdvertisements, pageable, advertisementPage.getTotalElements());

        // ApiResponse 반환
        return ApiResponse.success(HttpStatus.OK, "활성 광고 목록을 성공적으로 조회했습니다.", sortedPage);
    }


    /**
     * 특정 사용자에 대한 광고 참가 조건 검증.
     *
     * @param advertisementId 광고 ID
     * @param userId 사용자 ID
     * @return 조건을 만족하면 true
     * @throws InvalidQualificationException 조건을 만족하지 못하면 예외 발생
     */
    @Transactional(readOnly = true)
    public boolean canUserParticipate(UUID advertisementId, UUID userId) {
        Advertisement advertisement = advertisementRepository.findById(advertisementId)
                .orElseThrow(() -> new AdvertisementNotFoundException("광고를 찾을 수 없습니다."));

        QualificationCriteria criteria = parseQualificationCriteria(advertisement.getQualificationCriteria());

        if (!qualificationValidator.isValid(criteria, userId)) {
            throw new InvalidQualificationException("사용자가 광고 참가 자격 조건을 만족하지 못합니다.");
        }

        return true;
    }

    /**
     * Redis에서 광고 제목 중복 여부를 확인합니다.
     *
     * @param title 광고 제목
     * @return 제목이 이미 존재하면 true, 아니면 false
     */
    private boolean isAdvertisementTitleExists(String title) {
        String cacheKey = "advertisement_title:" + title;
        if (redisTemplate.hasKey(cacheKey)) {
            return true;
        }

        // DB에서 제목 중복 확인
        boolean existsInDb = advertisementRepository.findByTitle(title).isPresent();
        if (existsInDb) {
            redisTemplate.opsForValue().set(cacheKey, "exists", 10, TimeUnit.MINUTES); // Redis 캐시에 저장
        }

        return existsInDb;
    }

    /**
     * 광고 데이터 검증 로직.
     *
     * @param advertisement 검증할 광고 엔티티
     * @throws InvalidAdvertisementException 광고 데이터가 유효하지 않을 경우 예외 발생
     */
    private void validateAdvertisement(Advertisement advertisement) {
        // 노출 시작일이 종료일보다 늦는 경우 예외 발생
        if (advertisement.getExposureStartDate().isAfter(advertisement.getExposureEndDate())) {
            throw new InvalidAdvertisementException("광고 노출 시작일은 종료일보다 앞서야 합니다.");
        }

        // 자격 조건 검증
        if (advertisement.getQualificationCriteria() != null) {
            validateQualificationCriteria(advertisement.getQualificationCriteria());
        }
    }

    /**
     * 광고 참가 자격 조건을 검증합니다.
     *
     * @param criteria 자격 조건 JSON 문자열
     * @throws InvalidAdvertisementException 광고 참가 자격 조건이 유효하지 않을 경우 예외 발생
     */
    private void validateQualificationCriteria(String criteria) {
        try {
            // JSON 형식 검증
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.readTree(criteria);
        } catch (JsonProcessingException e) {
            throw new InvalidAdvertisementException("유효하지 않은 광고 참가 자격 조건(JSON)입니다.");
        }
    }

    /**
     * 광고 참가 조건을 JSON 문자열에서 QualificationCriteria 객체로 변환합니다.
     *
     * @param criteriaJson JSON 문자열
     * @return QualificationCriteria 객체
     */
    private QualificationCriteria parseQualificationCriteria(String criteriaJson) {
        if (criteriaJson == null || criteriaJson.isEmpty()) {
            return null; // 조건이 없을 경우 null 반환
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(criteriaJson, QualificationCriteria.class);
        } catch (JsonProcessingException e) {
            throw new InvalidAdvertisementException("유효하지 않은 자격 조건 JSON 형식입니다.");
        }
    }

    /**
     * 광고 정보를 Redis 캐시에 저장합니다.
     *
     * @param advertisement 저장할 광고 엔티티
     */
    private void cacheAdvertisement(Advertisement advertisement) {
        String cacheKey = "advertisement:title:" + advertisement.getTitle();
        redisTemplate.opsForValue().set(cacheKey, advertisement.getId().toString(), Duration.ofHours(1)); // TTL 1시간
    }
}
