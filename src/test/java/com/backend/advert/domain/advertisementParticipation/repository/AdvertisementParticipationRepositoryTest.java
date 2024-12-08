package com.backend.advert.domain.advertisementParticipation.repository;

import com.backend.advert.domain.advertisement.entity.Advertisement;
import com.backend.advert.domain.advertisement.repository.AdvertisementRepository;
import com.backend.advert.domain.advertisementParticipation.entity.AdvertisementParticipation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY) // 인메모리 DB 사용
@ActiveProfiles("test")
class AdvertisementParticipationRepositoryTest {

    @Autowired
    private AdvertisementParticipationRepository participationRepository;

    @Autowired
    private AdvertisementRepository advertisementRepository;

    private Advertisement testAdvertisement;
    private UUID testUserId;
    private static final LocalDateTime TEST_DATE = LocalDateTime.of(2024, 11, 22, 12, 0, 0);

    @BeforeEach
    void setUp() {
        // 데이터 초기화
        participationRepository.deleteAll();
        advertisementRepository.deleteAll();

        // 사용자 ID 생성
        testUserId = UUID.randomUUID();

        // 광고 생성
        testAdvertisement = advertisementRepository.save(
                new Advertisement(
                        "테스트 광고",
                        1000,
                        10,
                        "테스트 광고 설명",
                        "http://example.com/image.jpg",
                        TEST_DATE.minusDays(1), // 어제 시작
                        TEST_DATE.plusDays(10), // 10일 후 종료
                        null
                )
        );

        // 광고 참여 데이터 생성
        AdvertisementParticipation participation = new AdvertisementParticipation(
                testAdvertisement,
                testUserId,
                TEST_DATE
        );
        participationRepository.save(participation);
    }

    @Test
    @DisplayName("특정 사용자의 광고 참여 이력을 조회한다.")
    void testFindByUserId() {
        List<AdvertisementParticipation> participations = participationRepository.findByUserId(testUserId);

        assertThat(participations).hasSize(1);
        assertThat(participations.get(0).getAdvertisement().getId()).isEqualTo(testAdvertisement.getId());
        assertThat(participations.get(0).getUserId()).isEqualTo(testUserId);
    }

    @Test
    @DisplayName("특정 광고에 대한 참여 이력을 조회한다.")
    void testFindByAdvertisementId() {
        List<AdvertisementParticipation> participations = participationRepository.findByAdvertisementId(testAdvertisement.getId());

        assertThat(participations).hasSize(1);
        assertThat(participations.get(0).getUserId()).isEqualTo(testUserId);
        assertThat(participations.get(0).getAdvertisement().getId()).isEqualTo(testAdvertisement.getId());
    }

    @Test
    @DisplayName("사용자가 특정 광고에 이미 참여했는지 확인한다.")
    void testExistsByUserIdAndAdvertisementId() {
        boolean exists = participationRepository.existsByUserIdAndAdvertisementId(testUserId, testAdvertisement.getId());

        assertTrue(exists, "사용자가 해당 광고에 이미 참여했어야 합니다.");
    }

    @Test
    @DisplayName("참여 이력이 없는 사용자로 조회할 때 빈 리스트를 반환한다.")
    void testFindByUserId_NoParticipation() {
        UUID nonExistingUserId = UUID.randomUUID();
        List<AdvertisementParticipation> participations = participationRepository.findByUserId(nonExistingUserId);

        assertThat(participations).isEmpty();
    }

    @Test
    @DisplayName("참여 이력이 없는 광고로 조회할 때 빈 리스트를 반환한다.")
    void testFindByAdvertisementId_NoParticipation() {
        UUID nonExistingAdvertisementId = UUID.randomUUID();
        List<AdvertisementParticipation> participations = participationRepository.findByAdvertisementId(nonExistingAdvertisementId);

        assertThat(participations).isEmpty();
    }

    @Test
    @DisplayName("참여하지 않은 사용자가 특정 광고에 참여 여부를 확인할 때 false를 반환한다.")
    void testExistsByUserIdAndAdvertisementId_NoParticipation() {
        UUID nonParticipatingUserId = UUID.randomUUID();
        boolean exists = participationRepository.existsByUserIdAndAdvertisementId(nonParticipatingUserId, testAdvertisement.getId());

        assertFalse(exists, "참여하지 않은 사용자에 대해 false를 반환해야 합니다.");
    }
}
