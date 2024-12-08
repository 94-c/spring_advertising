package com.backend.advert.domain.advertisement.dto;

import com.backend.advert.domain.advertisement.entity.Advertisement;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
public class CreateAdvertisementRequest {

    @NotBlank(message = "광고 제목은 필수입니다.")
    private String title;

    @NotNull(message = "보상 금액은 필수입니다.")
    @Positive(message = "보상 금액은 0보다 커야 합니다.")
    private Integer rewardAmount;

    @NotNull(message = "최대 참여 가능 횟수는 필수입니다.")
    @Positive(message = "최대 참여 가능 횟수는 0보다 커야 합니다.")
    private Integer maxParticipationCount;

    @NotBlank(message = "광고 설명은 필수입니다.")
    private String description;

    @NotBlank(message = "이미지 URL은 필수입니다.")
    private String imageUrl;

    @NotNull(message = "광고 노출 시작일은 필수입니다.")
    private LocalDateTime exposureStartDate;

    @NotNull(message = "광고 노출 종료일은 필수입니다.")
    private LocalDateTime exposureEndDate;

    private String qualificationCriteria; // 선택 필드

    public CreateAdvertisementRequest(String title, Integer rewardAmount, Integer maxParticipationCount, String description, String imageUrl, LocalDateTime exposureStartDate, LocalDateTime exposureEndDate, String qualificationCriteria) {
        this.title = title;
        this.rewardAmount = rewardAmount;
        this.maxParticipationCount = maxParticipationCount;
        this.description = description;
        this.imageUrl = imageUrl;
        this.exposureStartDate = exposureStartDate;
        this.exposureEndDate = exposureEndDate;
        this.qualificationCriteria = qualificationCriteria;
    }

    /**
     * CreateAdvertisementRequest를 Advertisement 엔티티로 변환합니다.
     *
     * @return Advertisement 엔티티
     */
    public Advertisement toEntity() {
        return new Advertisement(
                title,
                rewardAmount,
                maxParticipationCount,
                description,
                imageUrl,
                exposureStartDate,
                exposureEndDate,
                qualificationCriteria
        );
    }
}

