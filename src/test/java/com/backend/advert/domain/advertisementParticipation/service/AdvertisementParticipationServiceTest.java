package com.backend.advert.domain.advertisementParticipation.service;

import com.backend.advert.domain.advertisement.entity.Advertisement;
import com.backend.advert.domain.advertisement.exception.InvalidAdvertisementException;
import com.backend.advert.domain.advertisementParticipation.dto.AdvertisementParticipationRequest;
import com.backend.advert.domain.advertisementParticipation.repository.AdvertisementParticipationRepository;
import com.backend.advert.domain.advertisement.repository.AdvertisementRepository;
import com.backend.advert.common.response.ApiResponse;
import com.backend.advert.config.redis.RedisLockService;
import com.backend.advert.domain.point.exception.PointServiceException;
import com.backend.advert.domain.point.service.PointService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdvertisementParticipationServiceTest {

    @InjectMocks
    private AdvertisementParticipationService participationService;

    @Mock
    private AdvertisementRepository advertisementRepository;

    @Mock
    private AdvertisementParticipationRepository participationRepository;

    @Mock
    private RedisLockService redisLockService;

    @Mock
    private PointService pointService;

    private static final UUID TEST_ADVERTISEMENT_ID = UUID.randomUUID();
    private static final UUID TEST_USER_ID = UUID.randomUUID();

    @Test
    @DisplayName("광고 참여 실패 테스트 - 남은 참여 가능 횟수 없음")
    void testParticipateInAdvertisement_NoParticipationCount() {
        // Given
        AdvertisementParticipationRequest request = new AdvertisementParticipationRequest(TEST_ADVERTISEMENT_ID, TEST_USER_ID);

        Advertisement advertisement = new Advertisement(
                "테스트 광고",
                100,
                0,
                "테스트 설명",
                "http://test.image.url",
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                "{}"
        );
        advertisement.setId(TEST_ADVERTISEMENT_ID);

        when(advertisementRepository.findById(TEST_ADVERTISEMENT_ID)).thenReturn(Optional.of(advertisement));
        doNothing().when(redisLockService).lock(anyString());
        doNothing().when(redisLockService).unlock(anyString());

        // When & Then
        InvalidAdvertisementException exception = assertThrows(
                InvalidAdvertisementException.class,
                () -> participationService.participateInAdvertisement(request)
        );

        assertEquals("남은 참여 가능 횟수가 없습니다.", exception.getMessage());
        verify(redisLockService, times(1)).unlock(anyString());
        verify(participationRepository, never()).save(any());
    }

    @Test
    @DisplayName("광고 참여 성공 테스트")
    void testParticipateInAdvertisement_Success() {
        // Given
        AdvertisementParticipationRequest request = new AdvertisementParticipationRequest(TEST_ADVERTISEMENT_ID, TEST_USER_ID);

        Advertisement advertisement = new Advertisement(
                "테스트 광고",
                100,
                5,
                "테스트 설명",
                "http://test.image.url",
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                "{}"
        );
        advertisement.setId(TEST_ADVERTISEMENT_ID);

        when(advertisementRepository.findById(TEST_ADVERTISEMENT_ID)).thenReturn(Optional.of(advertisement));
        when(participationRepository.existsByUserIdAndAdvertisementId(TEST_USER_ID, TEST_ADVERTISEMENT_ID)).thenReturn(false);
        doNothing().when(redisLockService).lock(anyString());
        doNothing().when(redisLockService).unlock(anyString());
        doNothing().when(pointService).addPoints(eq(TEST_USER_ID), eq(100));

        // When
        ApiResponse<String> response = participationService.participateInAdvertisement(request);

        // Then
        assertNotNull(response);
        assertEquals("광고 참여가 완료되었습니다.", response.getMessage());
        verify(participationRepository, times(1)).save(any());
        verify(pointService, times(1)).addPoints(TEST_USER_ID, 100);
        verify(redisLockService, times(1)).unlock(anyString());
    }

    @Test
    @DisplayName("광고 참여 실패 테스트 - 포인트 적립 실패")
    void testParticipateInAdvertisement_PointServiceFailure() {
        // Given
        AdvertisementParticipationRequest request = new AdvertisementParticipationRequest(TEST_ADVERTISEMENT_ID, TEST_USER_ID);

        Advertisement advertisement = new Advertisement(
                "테스트 광고",
                100,
                5,
                "테스트 설명",
                "http://test.image.url",
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                "{}"
        );
        advertisement.setId(TEST_ADVERTISEMENT_ID);

        when(advertisementRepository.findById(TEST_ADVERTISEMENT_ID)).thenReturn(Optional.of(advertisement));
        when(participationRepository.existsByUserIdAndAdvertisementId(TEST_USER_ID, TEST_ADVERTISEMENT_ID)).thenReturn(false);
        doNothing().when(redisLockService).lock(anyString());
        doNothing().when(redisLockService).unlock(anyString());
        doThrow(new PointServiceException("포인트 적립 실패"))
                .when(pointService).addPoints(eq(TEST_USER_ID), eq(100));

        // When
        ApiResponse<String> response = participationService.participateInAdvertisement(request);

        // Then
        assertNotNull(response);
        assertEquals("광고 참여는 완료되었으나 포인트 적립에 실패했습니다.", response.getMessage());
        verify(participationRepository, times(1)).save(any());
        verify(pointService, times(1)).addPoints(TEST_USER_ID, 100);
        verify(redisLockService, times(1)).unlock(anyString());
    }

    @Test
    @DisplayName("광고 참여 실패 테스트 - 이미 참여한 광고")
    void testParticipateInAdvertisement_AlreadyParticipated() {
        // Given
        AdvertisementParticipationRequest request = new AdvertisementParticipationRequest(TEST_ADVERTISEMENT_ID, TEST_USER_ID);

        Advertisement advertisement = new Advertisement(
                "테스트 광고",
                100,
                5,
                "테스트 설명",
                "http://test.image.url",
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                "{}"
        );
        advertisement.setId(TEST_ADVERTISEMENT_ID);

        when(advertisementRepository.findById(TEST_ADVERTISEMENT_ID)).thenReturn(Optional.of(advertisement));
        when(participationRepository.existsByUserIdAndAdvertisementId(TEST_USER_ID, TEST_ADVERTISEMENT_ID)).thenReturn(true);
        doNothing().when(redisLockService).lock(anyString());
        doNothing().when(redisLockService).unlock(anyString());

        // When & Then
        InvalidAdvertisementException exception = assertThrows(
                InvalidAdvertisementException.class,
                () -> participationService.participateInAdvertisement(request)
        );

        assertEquals("이미 참여한 광고입니다.", exception.getMessage());
        verify(redisLockService, times(1)).unlock(anyString());
    }
}
