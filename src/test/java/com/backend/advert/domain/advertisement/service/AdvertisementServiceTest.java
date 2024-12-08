package com.backend.advert.domain.advertisement.service;

import com.backend.advert.common.response.ApiResponse;
import com.backend.advert.common.validator.QualificationValidator;
import com.backend.advert.domain.advertisement.dto.AdvertisementResponse;
import com.backend.advert.domain.advertisement.dto.CreateAdvertisementRequest;
import com.backend.advert.domain.advertisement.entity.Advertisement;
import com.backend.advert.domain.advertisement.exception.AdvertisementAlreadyExistsException;
import com.backend.advert.domain.advertisement.exception.InvalidQualificationException;
import com.backend.advert.domain.advertisement.repository.AdvertisementRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdvertisementServiceTest {

    @Mock
    private AdvertisementRepository advertisementRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private QualificationValidator qualificationValidator;

    @InjectMocks
    private AdvertisementService advertisementService;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    private static final LocalDateTime FIXED_NOW = LocalDateTime.of(2024, 11, 22, 12, 0, 0, 0);

    @Test
    @DisplayName("광고 저장 성공 테스트 - Redis 캐시 사용 확인")
    void testSaveAdvertisement_Success() {
        // Given: 광고 생성 요청과 저장될 광고 데이터
        CreateAdvertisementRequest request = createTestAdvertisementRequest();
        Advertisement savedAdvertisement = createTestAdvertisement(UUID.randomUUID());

        // Mock 동작 설정
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.hasKey(anyString())).thenReturn(false);
        when(advertisementRepository.findByTitle(anyString())).thenReturn(Optional.empty());
        when(advertisementRepository.save(any(Advertisement.class))).thenReturn(savedAdvertisement);

        // Redis 캐시 저장 동작 Mock
        doNothing().when(valueOperations).set(
                eq("advertisement:title:테스트 광고"),
                eq(savedAdvertisement.getId().toString()),
                eq(Duration.ofHours(1))
        );

        // When: 광고 저장 실행
        ApiResponse<AdvertisementResponse> response = advertisementService.saveAdvertisement(request);

        // Then: 응답 검증 및 캐시 저장 확인
        assertEquals(HttpStatus.CREATED, response.getStatus());
        assertEquals("테스트 광고", response.getData().getTitle());
        verify(redisTemplate, times(1)).opsForValue();
        verify(valueOperations, times(1)).set(
                eq("advertisement:title:테스트 광고"),
                eq(savedAdvertisement.getId().toString()),
                eq(Duration.ofHours(1))
        );
    }

    @Test
    @DisplayName("광고 제목 중복 예외 테스트")
    void testSaveAdvertisement_AlreadyExists() {
        // Given: 광고 제목이 중복된 요청
        CreateAdvertisementRequest request = createTestAdvertisementRequest();

        // Redis에 중복된 제목이 있다고 Mock
        when(redisTemplate.hasKey(anyString())).thenReturn(true);

        // When & Then: 예외 발생 확인
        assertThrows(AdvertisementAlreadyExistsException.class, () -> advertisementService.saveAdvertisement(request));
    }

    @Test
    @DisplayName("활성 광고 목록 조회 성공 테스트")
    void testGetActiveAdvertisements_Success() {
        // Given: 활성 광고와 페이지 설정
        Advertisement advertisement = createTestAdvertisement(UUID.randomUUID());
        Pageable pageable = PageRequest.of(0, 10);

        // 활성 광고 목록 반환 Mock
        when(advertisementRepository.findByExposureStartDateBeforeAndExposureEndDateAfterAndRemainingParticipationCountGreaterThan(
                any(LocalDateTime.class), any(LocalDateTime.class), eq(0), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(advertisement)));

        // When: 활성 광고 목록 조회
        ApiResponse<Page<AdvertisementResponse>> response = advertisementService.getActiveAdvertisements(0, 10);

        // Then: 반환된 광고 검증
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals(1, response.getData().getContent().size());
        assertEquals("테스트 광고", response.getData().getContent().get(0).getTitle());
    }

    @Test
    @DisplayName("광고 참여 가능 성공 테스트")
    void testCanUserParticipate_Success() {
        // Given: 광고 ID와 사용자 ID
        UUID advertisementId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Advertisement advertisement = createTestAdvertisement(advertisementId);

        // Mock 설정
        when(advertisementRepository.findById(any(UUID.class))).thenReturn(Optional.of(advertisement));
        when(qualificationValidator.isValid(any(), eq(userId))).thenReturn(true);

        // When: 광고 참여 가능 여부 확인
        boolean result = advertisementService.canUserParticipate(advertisementId, userId);

        // Then: 참여 가능 확인
        assertTrue(result);
    }

    @Test
    @DisplayName("광고 참여 자격 미달 테스트")
    void testCanUserParticipate_InvalidQualification() {
        // Given: 광고 ID와 사용자 ID
        UUID advertisementId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Advertisement advertisement = createTestAdvertisement(advertisementId);

        // Mock 설정: 자격 미달
        when(advertisementRepository.findById(any(UUID.class))).thenReturn(Optional.of(advertisement));
        when(qualificationValidator.isValid(any(), eq(userId))).thenReturn(false);

        // When & Then: 자격 미달 예외 확인
        assertThrows(InvalidQualificationException.class, () -> advertisementService.canUserParticipate(advertisementId, userId));
    }

    // 테스트 데이터 생성 메서드
    private CreateAdvertisementRequest createTestAdvertisementRequest() {
        return new CreateAdvertisementRequest(
                "테스트 광고",
                1000,
                10,
                "테스트 설명",
                "http://example.com/image.jpg",
                FIXED_NOW,
                FIXED_NOW.plusDays(7),
                null
        );
    }

    private Advertisement createTestAdvertisement(UUID advertisementId) {
        Advertisement advertisement = new Advertisement(
                "테스트 광고",
                1000,
                10,
                "테스트 설명",
                "http://example.com/image.jpg",
                FIXED_NOW,
                FIXED_NOW.plusDays(7),
                null
        );
        ReflectionTestUtils.setField(advertisement, "id", advertisementId);
        return advertisement;
    }
}
