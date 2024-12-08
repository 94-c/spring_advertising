package com.backend.advert.domain.user.entity;

import com.backend.advert.domain.advertisement.entity.Advertisement;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Table(name = "users")
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private Integer participationCount; // 사용자의 총 광고 참여 횟수

    @ManyToMany(mappedBy = "participants")
    @Builder.Default
    private List<Advertisement> participatedAdvertisements = new ArrayList<>();

    public boolean hasParticipatedIn(UUID advertisementId) {
        return participatedAdvertisements.stream()
                .anyMatch(ad -> ad.getId().equals(advertisementId));
    }

}
