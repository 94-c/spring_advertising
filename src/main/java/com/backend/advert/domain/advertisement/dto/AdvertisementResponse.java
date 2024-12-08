package com.backend.advert.domain.advertisement.dto;

import com.backend.advert.domain.advertisement.entity.Advertisement;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class AdvertisementResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private final UUID id;
    private final String title;
    private final Integer rewardPoints;
    private final Integer remainingParticipationCount;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") // ISO 8601 형식 적용
    private final LocalDateTime exposureStartDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private final LocalDateTime exposureEndDate;

    public AdvertisementResponse(UUID id, String title, Integer rewardPoints, Integer remainingParticipationCount,
                                 LocalDateTime exposureStartDate, LocalDateTime exposureEndDate) {
        this.id = id;
        this.title = title;
        this.rewardPoints = rewardPoints;
        this.remainingParticipationCount = remainingParticipationCount;
        this.exposureStartDate = exposureStartDate;
        this.exposureEndDate = exposureEndDate;
    }

    /**
     *
     * @param advertisement Advertisement 엔티티
     * @return AdvertisementResponse
     */
    public static AdvertisementResponse fromEntity(Advertisement advertisement) {
        return new AdvertisementResponse(
                advertisement.getId(),
                advertisement.getTitle(),
                advertisement.getRewardPoints(), // 엔티티 필드명 통일
                advertisement.getRemainingParticipationCount(),
                advertisement.getExposureStartDate(),
                advertisement.getExposureEndDate()
        );
    }
}
