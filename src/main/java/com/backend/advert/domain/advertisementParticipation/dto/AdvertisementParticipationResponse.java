package com.backend.advert.domain.advertisementParticipation.dto;

import com.backend.advert.domain.advertisementParticipation.entity.AdvertisementParticipation;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class AdvertisementParticipationResponse {
    private UUID advertisementId;
    private String advertisementTitle;
    private UUID userId;
    private Integer rewardPoints;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime participationTime;

    public AdvertisementParticipationResponse(UUID advertisementId, String advertisementTitle, UUID userId, Integer rewardPoints, LocalDateTime participationTime) {
        this.advertisementId = advertisementId;
        this.advertisementTitle = advertisementTitle;
        this.userId = userId;
        this.rewardPoints = rewardPoints;
        this.participationTime = participationTime;
    }

    public static AdvertisementParticipationResponse fromEntity(AdvertisementParticipation participation) {
        return new AdvertisementParticipationResponse(
                participation.getAdvertisement().getId(),
                participation.getAdvertisement().getTitle(),
                participation.getUserId(),
                participation.getAdvertisement().getRewardPoints(),
                participation.getParticipatedAt()
        );
    }
}
