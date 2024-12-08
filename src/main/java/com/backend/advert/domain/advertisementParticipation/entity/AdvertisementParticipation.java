package com.backend.advert.domain.advertisementParticipation.entity;

import com.backend.advert.domain.advertisement.entity.Advertisement;
import com.backend.advert.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Table(name = "advertisement_participations")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdvertisementParticipation extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "advertisement_id", nullable = false)
    private Advertisement advertisement;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "participated_at", nullable = false)
    private LocalDateTime participatedAt;


    public AdvertisementParticipation(Advertisement advertisement, UUID userId, LocalDateTime participatedAt) {
        this.advertisement = advertisement;
        this.userId = userId;
        this.participatedAt = participatedAt;
    }
}
