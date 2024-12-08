package com.backend.advert.domain.controller;

import com.backend.advert.domain.advertisement.controller.AdvertisementController;
import com.backend.advert.domain.advertisement.dto.AdvertisementResponse;
import com.backend.advert.domain.advertisementParticipation.controller.AdvertisementParticipationController;
import com.backend.advert.domain.advertisement.dto.CreateAdvertisementRequest;
import com.backend.advert.domain.advertisementParticipation.dto.AdvertisementParticipationRequest;
import com.backend.advert.common.response.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ConcurrentControllerTest {

    @Mock
    private AdvertisementController advertisementController;

    @Mock
    private AdvertisementParticipationController participationController;

    @Test
    @DisplayName("동시성 테스트 (여러 명 동시에 요청)")
    void testConcurrentCreateAndParticipation() throws Exception {
        // Given
        UUID userId = UUID.randomUUID();
        UUID advertisementId = UUID.randomUUID();

        AtomicInteger successfulRequests = new AtomicInteger(0);  // 성공한 요청 수를 추적

        // 광고 생성 요청과 응답 mock 설정
        CreateAdvertisementRequest createRequest = new CreateAdvertisementRequest(
                "테스트 광고",
                1000,
                10,
                "테스트 설명",
                "http://example.com/image.jpg",
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(7),
                null
        );

        ApiResponse<AdvertisementResponse> advertisementResponse = ApiResponse.success(
                HttpStatus.CREATED,
                "광고가 성공적으로 저장되었습니다.",
                new AdvertisementResponse(advertisementId, "테스트 광고", 1000, 10, LocalDateTime.now(), LocalDateTime.now().plusDays(7))
        );

        when(advertisementController.createAdvertisement(createRequest))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(advertisementResponse));

        // 광고 참여 요청과 응답 mock 설정
        AdvertisementParticipationRequest participationRequest = new AdvertisementParticipationRequest(
                advertisementId,
                userId
        );
        ApiResponse<String> participationResponse = ApiResponse.success(HttpStatus.OK, "광고 참여 성공", "Participation Success");
        when(participationController.participateInAdvertisement(participationRequest))
                .thenReturn(ResponseEntity.status(HttpStatus.OK).body(participationResponse));

        // When
        // 100명의 사용자에 대해 동시 요청을 시뮬레이션
        int concurrentUsers = 100;
        CompletableFuture<Void>[] futures = new CompletableFuture[concurrentUsers];

        IntStream.range(0, concurrentUsers).forEach(i -> {
            // 광고 생성 비동기 요청
            futures[i] = CompletableFuture.supplyAsync(() -> advertisementController.createAdvertisement(createRequest))
                    .thenCompose(response -> CompletableFuture.supplyAsync(() -> participationController.participateInAdvertisement(participationRequest)))
                    .thenRun(() -> successfulRequests.incrementAndGet());  // 성공 시 카운트 증가
        });

        // 모든 요청이 완료될 때까지 기다림
        CompletableFuture.allOf(futures).join();

        // Then
        // 모든 요청에 대해 응답을 검증
        for (CompletableFuture<Void> future : futures) {
            future.join();  // 모든 CompletableFuture 완료 대기
        }

        // 성공한 요청 수를 출력
        System.out.println("성공적으로 요청을 처리한 사용자 수: " + successfulRequests.get());

        // 성공적인 요청 수가 100개임을 검증
        assertEquals(100, successfulRequests.get());
    }
}
