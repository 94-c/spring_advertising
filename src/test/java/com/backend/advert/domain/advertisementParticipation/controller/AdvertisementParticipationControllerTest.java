package com.backend.advert.domain.advertisementParticipation.controller;

import com.backend.advert.domain.advertisementParticipation.dto.AdvertisementParticipationRequest;
import com.backend.advert.domain.advertisementParticipation.dto.AdvertisementParticipationResponse;
import com.backend.advert.domain.advertisementParticipation.service.AdvertisementParticipationService;
import com.backend.advert.common.response.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdvertisementParticipationControllerTest {

    @Mock
    private AdvertisementParticipationService participationService;

    @InjectMocks
    private AdvertisementParticipationController participationController;

    private static final UUID TEST_USER_ID = UUID.randomUUID();
    private static final UUID TEST_ADVERTISEMENT_ID = UUID.randomUUID();
    private static final LocalDateTime FIXED_NOW = LocalDateTime.of(2024, 11, 23, 12, 0, 0);

    /**
     * 광고 참여 성공 테스트
     */
    @Test
    @DisplayName("광고 참여 성공 테스트")
    void testParticipateInAdvertisement_Success() {
        // Given
        AdvertisementParticipationRequest request = new AdvertisementParticipationRequest(TEST_ADVERTISEMENT_ID, TEST_USER_ID);
        ApiResponse<String> expectedResponse = ApiResponse.success(HttpStatus.OK, "광고 참여가 완료되었습니다.", null);

        when(participationService.participateInAdvertisement(any(AdvertisementParticipationRequest.class))).thenReturn(expectedResponse);

        // When
        ResponseEntity<ApiResponse<String>> response = participationController.participateInAdvertisement(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("광고 참여가 완료되었습니다.", response.getBody().getMessage());
        verify(participationService, times(1)).participateInAdvertisement(any(AdvertisementParticipationRequest.class));
    }

    /**
     * 광고 참여 이력 조회 성공 테스트
     */
    @Test
    @DisplayName("광고 참여 이력 조회 성공 테스트")
    void testGetParticipationHistory_Success() {
        // Given
        AdvertisementParticipationResponse participationResponse = new AdvertisementParticipationResponse(
                TEST_ADVERTISEMENT_ID,
                "테스트 광고",
                TEST_USER_ID,
                1000,
                FIXED_NOW
        );
        Page<AdvertisementParticipationResponse> participationPage = new PageImpl<>(
                Collections.singletonList(participationResponse)
        );
        ApiResponse<Page<AdvertisementParticipationResponse>> expectedResponse =
                ApiResponse.success(HttpStatus.OK, "광고 참여 이력을 성공적으로 조회했습니다.", participationPage);

        when(participationService.getParticipationHistory(
                eq(TEST_USER_ID),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                eq(0),
                eq(50)
        )).thenReturn(expectedResponse);

        // When
        ResponseEntity<ApiResponse<Page<AdvertisementParticipationResponse>>> response = participationController.getParticipationHistory(
                TEST_USER_ID,
                LocalDate.of(2024, 11, 1),
                LocalDate.of(2024, 11, 30),
                0,
                50
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getData().getContent().size());
        assertEquals("테스트 광고", response.getBody().getData().getContent().get(0).getAdvertisementTitle());
        verify(participationService, times(1)).getParticipationHistory(
                eq(TEST_USER_ID),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                eq(0),
                eq(50)
        );
    }
}
