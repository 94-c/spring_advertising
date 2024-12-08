package com.backend.advert.domain.advertisementParticipation.controller;

import com.backend.advert.domain.advertisementParticipation.dto.AdvertisementParticipationRequest;
import com.backend.advert.domain.advertisementParticipation.dto.AdvertisementParticipationResponse;
import com.backend.advert.domain.advertisementParticipation.service.AdvertisementParticipationService;
import com.backend.advert.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/advertisements/participations")
@RequiredArgsConstructor
public class AdvertisementParticipationController {

    private final AdvertisementParticipationService participationService;

    /**
     * 광고 참여 API
     * @param request 광고 참여 요청 DTO
     * @return 성공 메시지
     */
    @PostMapping
    public ResponseEntity<ApiResponse<String>> participateInAdvertisement(
            @RequestBody @Valid AdvertisementParticipationRequest request) {
        ApiResponse<String> response = participationService.participateInAdvertisement(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * 광고 참여 이력 조회 API
     * - 유저가 특정 기간 동안 참여한 광고 이력을 조회합니다.
     * - 참여 이력은 광고 참여 시각이 오래된 순으로 정렬됩니다.
     * - 페이지네이션을 적용하여 최대 50개의 이력을 한 번에 조회할 수 있습니다.
     *
     * @param userId 사용자 ID
     * @param startDate 조회 시작 날짜
     * @param endDate 조회 종료 날짜
     * @param page 조회할 페이지 번호
     * @param size 한 번에 조회할 이력 수 (최대 50)
     * @return 광고 참여 이력 리스트 응답 DTO
     */
    @GetMapping("/{userId}/history")
    public ResponseEntity<ApiResponse<Page<AdvertisementParticipationResponse>>> getParticipationHistory(
            @PathVariable UUID userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        ApiResponse<Page<AdvertisementParticipationResponse>> response = participationService.getParticipationHistory(userId, startDate.atStartOfDay(), endDate.atTime(23, 59, 59), page, size);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
