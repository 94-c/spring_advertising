package com.backend.advert.domain.advertisement.controller;

import com.backend.advert.domain.advertisement.dto.AdvertisementResponse;
import com.backend.advert.domain.advertisement.dto.CreateAdvertisementRequest;
import com.backend.advert.domain.advertisement.exception.InvalidQualificationException;
import com.backend.advert.domain.advertisement.service.AdvertisementService;
import com.backend.advert.common.response.ApiResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdvertisementControllerTest {

    @Mock
    private AdvertisementService advertisementService;

    @InjectMocks
    private AdvertisementController advertisementController;

    private static final LocalDateTime FIXED_NOW = LocalDateTime.of(2024, 11, 22, 12, 0, 0, 0);

    /**
     * 광고 생성 성공 테스트
     */
    @Test
    @DisplayName("광고 생성 성공 테스트")
    void testCreateAdvertisement_Success() {
        // Given
        CreateAdvertisementRequest request = createTestAdvertisementRequest();
        AdvertisementResponse expectedResponse = createTestAdvertisementResponse(UUID.randomUUID());

        ApiResponse<AdvertisementResponse> apiResponse = ApiResponse.success(
                HttpStatus.CREATED,
                "광고가 성공적으로 저장되었습니다.",
                expectedResponse
        );

        when(advertisementService.saveAdvertisement(any(CreateAdvertisementRequest.class))).thenReturn(apiResponse);

        // When
        ResponseEntity<ApiResponse<AdvertisementResponse>> response = advertisementController.createAdvertisement(request);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("테스트 광고", response.getBody().getData().getTitle());
    }

    /**
     * 광고 제목 중복 예외 테스트
     */
    @Test
    @DisplayName("광고 제목 중복 예외 테스트")
    void testSaveAdvertisement_AlreadyExists() {
        // Given
        CreateAdvertisementRequest request = createTestAdvertisementRequest();

        when(advertisementService.saveAdvertisement(any(CreateAdvertisementRequest.class)))
                .thenThrow(new IllegalArgumentException("이미 존재하는 광고 제목입니다."));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> advertisementController.createAdvertisement(request));
    }

    /**
     * 활성 광고 목록 조회 성공 테스트
     */
    @Test
    @DisplayName("활성 광고 목록 조회 성공 테스트")
    void testGetActiveAdvertisements_Success() {
        // Given
        AdvertisementResponse expectedResponse = createTestAdvertisementResponse(UUID.randomUUID());
        ApiResponse<Page<AdvertisementResponse>> apiResponse = ApiResponse.success(
                HttpStatus.OK,
                "활성 광고 목록을 성공적으로 조회했습니다.",
                new PageImpl<>(Collections.singletonList(expectedResponse))
        );

        when(advertisementService.getActiveAdvertisements(anyInt(), anyInt())).thenReturn(apiResponse);

        // When
        ResponseEntity<ApiResponse<Page<AdvertisementResponse>>> response = advertisementController.getActiveAdvertisements(0, 10);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getData().getContent().size());
        assertEquals("테스트 광고", response.getBody().getData().getContent().get(0).getTitle());
    }

    /**
     * 광고 참여 가능 성공 테스트
     */
    @Test
    @DisplayName("광고 참여 가능 성공 테스트")
    void testCanUserParticipate_Success() {
        // Given
        UUID advertisementId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(advertisementService.canUserParticipate(advertisementId, userId)).thenReturn(true);

        // When
        boolean result = advertisementService.canUserParticipate(advertisementId, userId);

        // Then
        assertTrue(result);
    }

    /**
     * 광고 참여 자격 예외 테스트
     */
    @Test
    @DisplayName("광고 참여 자격 예외 테스트")
    void testCanUserParticipate_InvalidQualification() {
        // Given
        UUID advertisementId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(advertisementService.canUserParticipate(advertisementId, userId)).thenThrow(InvalidQualificationException.class);

        // When & Then
        assertThrows(InvalidQualificationException.class, () -> advertisementService.canUserParticipate(advertisementId, userId));
    }

    /**
     * 재사용을 위한 테스트 데이터 생성 메서드: CreateAdvertisementRequest
     */
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

    /**
     * 재사용을 위한 테스트 데이터 생성 메서드: AdvertisementResponse
     */
    private AdvertisementResponse createTestAdvertisementResponse(UUID advertisementId) {
        return new AdvertisementResponse(
                advertisementId,
                "테스트 광고",
                1000,
                10,
                FIXED_NOW,
                FIXED_NOW.plusDays(7)
        );
    }
}
