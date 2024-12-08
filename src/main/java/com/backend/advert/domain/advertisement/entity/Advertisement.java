package com.backend.advert.domain.advertisement.entity;

import com.backend.advert.common.entity.BaseTimeEntity;
import com.backend.advert.domain.advertisement.exception.InvalidAdvertisementException;
import com.backend.advert.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Table(name = "advertisements")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Advertisement extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Comment("광고의 고유 ID")
    private UUID id;

    @Column(unique = true, nullable = false)
    @Comment("광고명 (중복 불가)")
    private String title;

    @Column(name = "reward_points", nullable = false)
    @Comment("광고 참여 시 적립 포인트")
    private Integer rewardPoints;

    @Column(name = "max_participation_count", nullable = false)
    @Comment("광고 최대 참여 가능 횟수")
    private Integer maxParticipationCount;

    @Column(name = "remaining_participation_count", nullable = false)
    @Comment("광고 남은 참여 가능 횟수")
    private Integer remainingParticipationCount;

    @Lob
    @Column(name = "description", nullable = false)
    @Comment("광고 설명 문구")
    private String description;

    @Column(name = "image_url", nullable = false)
    @Comment("광고 이미지 URL")
    private String imageUrl;

    @Column(name = "exposure_start_date", nullable = false)
    @Comment("광고 노출 시작 일자")
    private LocalDateTime exposureStartDate;

    @Column(name = "exposure_end_date", nullable = false)
    @Comment("광고 노출 종료 일자")
    private LocalDateTime exposureEndDate;

    @Lob
    @Column(name = "qualification_criteria")
    @Comment("광고 참가 자격 (JSON 형식)")
    private String qualificationCriteria;

    @ManyToMany
    @JoinTable(
            name = "advertisement_participants",
            joinColumns = @JoinColumn(name = "advertisement_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> participants = new ArrayList<>();

    // 생성자
    public Advertisement(
            String title,
            Integer rewardPoints,
            Integer maxParticipationCount,
            String description,
            String imageUrl,
            LocalDateTime exposureStartDate,
            LocalDateTime exposureEndDate,
            String qualificationCriteria) {
        this.title = title;
        this.rewardPoints = rewardPoints;
        this.maxParticipationCount = maxParticipationCount;
        this.remainingParticipationCount = maxParticipationCount;
        this.description = description;
        this.imageUrl = imageUrl;
        this.exposureStartDate = exposureStartDate;
        this.exposureEndDate = exposureEndDate;
        this.qualificationCriteria = qualificationCriteria;
    }

    /**
     * 남은 참여 가능 횟수 감소
     * @throws InvalidAdvertisementException 남은 횟수가 0일 경우
     */
    public void decrementParticipationCount() {
        if (remainingParticipationCount > 0) {
            this.remainingParticipationCount--;
        }
    }

    /**
     * 테스트용 ID 설정 메서드
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * 남은 참여 가능 횟수 업데이트 메서드
     */
    public void updateRemainingParticipationCount(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("남은 참여 가능 횟수는 음수일 수 없습니다.");
        }
        this.remainingParticipationCount = count;
    }
}

