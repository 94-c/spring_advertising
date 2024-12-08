package com.backend.advert.domain.point.client;

import com.backend.advert.domain.point.exception.ExternalApiException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ExternalPointApiClient {

    /**
     * 외부 API 호출 로직
     * @param userId 사용자 ID
     * @param points 적립 포인트
     * @throws ExternalApiException 외부 API 호출 실패 시 예외 발생
     */
    public void addPoints(UUID userId, int points) {
        try {
            // Mock 호출
            System.out.printf("External API 호출: User %s에게 %d 포인트 요청%n", userId, points);
        } catch (Exception e) {
            throw new ExternalApiException("외부 포인트 API 호출 실패");
        }
    }
}
