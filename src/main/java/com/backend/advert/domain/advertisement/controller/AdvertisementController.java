package com.backend.advert.domain.advertisement.controller;

import com.backend.advert.domain.advertisement.dto.AdvertisementResponse;
import com.backend.advert.domain.advertisement.dto.CreateAdvertisementRequest;
import com.backend.advert.domain.advertisement.service.AdvertisementService;
import com.backend.advert.common.constants.PaginationConstants;
import com.backend.advert.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/advertisements")
@RequiredArgsConstructor
public class AdvertisementController {

    private final AdvertisementService advertisementService;

    /**
     * 새로운 광고를 생성합니다.
     *
     * @param request 광고 생성 요청 DTO
     * @return 생성된 광고의 응답 DTO를 포함한 성공 메시지
     */
    @PostMapping
    public ResponseEntity<ApiResponse<AdvertisementResponse>> createAdvertisement(
            @RequestBody @Valid CreateAdvertisementRequest request) {
        ApiResponse<AdvertisementResponse> response = advertisementService.saveAdvertisement(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 현재 활성 상태의 광고 목록을 조회합니다.
     * - 광고 참여 가능 횟수가 소진되지 않은 광고
     * - 노출 기간 내에 있는 광고
     * - 광고 참여 시 적립액수가 높은 순으로 조회 (최대 10개)
     *
     * @param page 페이지 번호 (기본값 0)
     * @param size 한 번에 조회할 광고의 수 (기본값 10)
     * @return 활성 상태의 광고 목록 응답 DTO를 포함한 성공 메시지
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<Page<AdvertisementResponse>>> getActiveAdvertisements(
            @RequestParam(defaultValue = "" + PaginationConstants.DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = "" + PaginationConstants.DEFAULT_SIZE) int size) {
        ApiResponse<Page<AdvertisementResponse>> response = advertisementService.getActiveAdvertisements(page, size);
        return ResponseEntity.ok(response);
    }
}
