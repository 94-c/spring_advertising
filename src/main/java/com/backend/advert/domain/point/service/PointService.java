package com.backend.advert.domain.point.service;

import com.backend.advert.domain.point.client.ExternalPointApiClient;
import com.backend.advert.domain.point.exception.ExternalApiException;
import com.backend.advert.domain.point.exception.PointServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PointService {

    // 외부 API 클라이언트를 통해 포인트 적립 요청을 처리
    private final ExternalPointApiClient externalPointApiClient;

    /**
     * 사용자에게 포인트를 적립합니다.
     *
     * @param userId 포인트를 적립할 대상 사용자의 UUID
     * @param points 적립할 포인트의 수
     * @throws PointServiceException 포인트 적립 과정에서 외부 API 호출 실패 시 발생
     */
    public void addPoints(UUID userId, int points) {
        try {
            // 외부 API 호출하여 포인트 적립
            externalPointApiClient.addPoints(userId, points);
            System.out.printf("User %s에게 %d 포인트가 성공적으로 적립되었습니다.%n", userId, points);
        } catch (ExternalApiException e) {
            // 외부 API 호출 실패 시 에러 로그 출력 및 사용자 정의 예외 발생
            System.err.printf("포인트 적립 실패: User %s, Points %d, Error: %s%n", userId, points, e.getMessage());
            throw new PointServiceException("포인트 적립에 실패했습니다.");
        }
    }
}
