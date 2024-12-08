package com.backend.advert.domain.point.service;

import com.backend.advert.domain.point.client.ExternalPointApiClient;
import com.backend.advert.domain.point.exception.ExternalApiException;
import com.backend.advert.domain.point.exception.PointServiceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {

    @InjectMocks
    private PointService pointService;

    @Mock
    private ExternalPointApiClient externalPointApiClient;

    private static final UUID TEST_USER_ID = UUID.randomUUID();
    private static final int TEST_POINTS = 100;

    @Test
    @DisplayName("포인트 적립 성공 테스트")
    void addPoints_Success() {
        // Given
        // ExternalPointApiClient는 아무 예외 없이 정상 동작한다고 가정

        // When
        pointService.addPoints(TEST_USER_ID, TEST_POINTS);

        // Then
        // ExternalPointApiClient의 addPoints 메서드가 한 번 호출되었는지 확인
        verify(externalPointApiClient, times(1)).addPoints(TEST_USER_ID, TEST_POINTS);
    }

    @Test
    @DisplayName("포인트 적립 실패 테스트 - 외부 API 호출 실패")
    void addPoints_ExternalApiFailure() {
        // Given
        // ExternalPointApiClient의 addPoints 호출 시 예외 발생 설정
        doThrow(new ExternalApiException("External API 호출 실패"))
                .when(externalPointApiClient).addPoints(TEST_USER_ID, TEST_POINTS);

        // When & Then
        PointServiceException exception = assertThrows(
                PointServiceException.class,
                () -> pointService.addPoints(TEST_USER_ID, TEST_POINTS)
        );

        // 예외 메시지가 정확히 포함되는지 확인
        assertEquals("포인트 적립에 실패했습니다.", exception.getMessage());

        // ExternalPointApiClient의 addPoints 메서드가 호출되었는지 확인
        verify(externalPointApiClient, times(1)).addPoints(TEST_USER_ID, TEST_POINTS);
    }
}
