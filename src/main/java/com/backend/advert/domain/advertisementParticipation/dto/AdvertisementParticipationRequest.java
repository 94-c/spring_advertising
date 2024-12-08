package com.backend.advert.domain.advertisementParticipation.dto;

import com.backend.advert.domain.advertisement.entity.Advertisement;
import com.backend.advert.domain.advertisementParticipation.entity.AdvertisementParticipation;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter
public class AdvertisementParticipationRequest {

    @NotNull(message = "광고 ID는 필수입니다.")
    private UUID advertisementId;

    @NotNull(message = "사용자 ID는 필수입니다.")
    private UUID userId;

    public AdvertisementParticipationRequest(UUID advertisementId, UUID userId) {
        this.advertisementId = advertisementId;
        this.userId = userId;
    }

    public AdvertisementParticipation toEntity(Advertisement advertisement) {
        return new AdvertisementParticipation(
                advertisement,
                userId,
                LocalDateTime.now()
        );
    }
}
