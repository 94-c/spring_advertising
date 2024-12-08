package com.backend.advert.domain.advertisement.repository;

import com.backend.advert.domain.advertisement.entity.Advertisement;
import com.backend.advert.domain.user.entity.User;
import com.backend.advert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY) // 인메모리 DB 사용
@ActiveProfiles("test")
class AdvertisementRepositoryTest {

    @Autowired
    private AdvertisementRepository advertisementRepository;

    @Autowired
    private UserRepository userRepository;

    private static final LocalDateTime FIXED_NOW = LocalDateTime.of(2024, 11, 22, 12, 0, 0, 0); // 고정 시간

    @BeforeEach
    void setUp() {
        advertisementRepository.deleteAll(); // 각 테스트 전에 데이터 초기화
        userRepository.deleteAll(); // 사용자 데이터 초기화
        createTestAdvertisements(); // 테스트 광고 데이터 삽입
    }

    private void createTestAdvertisements() {
        for (int i = 1; i <= 100; i++) {
            Advertisement advertisement = new Advertisement(
                    "광고 " + i,
                    1000 + i * 100,
                    10,
                    "광고 설명 " + i,
                    "http://example.com/image" + i + ".jpg",
                    FIXED_NOW.minusDays(10),  // 10일 전 시작
                    FIXED_NOW.plusDays(10),   // 10일 후 종료
                    null
            );
            advertisementRepository.save(advertisement);
        }
    }

    @Test
    @DisplayName("광고 제목을 기준으로 광고를 찾는 테스트")
    void testFindByTitle() {
        Optional<Advertisement> result = advertisementRepository.findByTitle("광고 1");

        assertTrue(result.isPresent());
        assertEquals("광고 1", result.get().getTitle());
    }

    @Test
    @DisplayName("광고 노출 날짜와 남은 참여 횟수를 기준으로 조회하는 테스트")
    void testFindByExposureDatesAndParticipationCount() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<Advertisement> activeAds = advertisementRepository
                .findByExposureStartDateBeforeAndExposureEndDateAfterAndRemainingParticipationCountGreaterThan(
                        FIXED_NOW, FIXED_NOW, 0, pageable);

        assertFalse(activeAds.isEmpty(), "활성 광고가 반환되어야 합니다.");
        assertEquals("광고 1", activeAds.getContent().get(0).getTitle(), "첫 번째 광고 제목이 일치해야 합니다.");
    }

    @Test
    @DisplayName("사용자가 광고에 참여할 수 있는지 확인하는 테스트")
    void testExistsByIdAndParticipantsContains() {
        User user = User.builder()
                .email("test@example.com")
                .username("테스트 사용자")
                .participationCount(0)
                .build();
        user = userRepository.save(user);

        Advertisement advertisement = new Advertisement(
                "참여된 광고",
                3000,
                30,
                "사용자가 참여한 광고입니다.",
                "http://example.com/participation.jpg",
                FIXED_NOW.minusDays(1),
                FIXED_NOW.plusDays(1),
                null
        );
        advertisement.getParticipants().add(user);  // 사용자를 참여자로 추가
        advertisementRepository.save(advertisement);

        boolean exists = advertisementRepository.existsByIdAndParticipantsContains(advertisement.getId(), user);

        assertTrue(exists, "사용자가 참여자로 포함되어 있어야 합니다.");
    }

    @Test
    @DisplayName("적어도 하나 이상의 광고가 조회되는 경우의 테스트")
    void testFindByExposureDatesAndReturnsResults() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // 비활성 광고 설정: FIXED_NOW 이후에 시작되는 광고로 설정
        Advertisement inactiveAdvertisement = new Advertisement(
                "비활성 광고",
                1000,
                10,
                "비활성 광고 설명",
                "http://example.com/inactive.jpg",
                FIXED_NOW.plusDays(1),  // FIXED_NOW 이후에 시작
                FIXED_NOW.plusDays(7),  // FIXED_NOW 이후에 종료
                null
        );
        advertisementRepository.save(inactiveAdvertisement);

        // When: 광고 조회
        Page<Advertisement> activeAds = advertisementRepository
                .findByExposureStartDateBeforeAndExposureEndDateAfterAndRemainingParticipationCountGreaterThan(
                        FIXED_NOW.minusDays(5), FIXED_NOW.plusDays(5), 0, pageable);

        // Then: 적어도 하나 이상의 광고가 조회되어야 한다.
        assertNotEquals(0, activeAds.getTotalElements(), "적어도 하나 이상의 광고가 조회되어야 한다.");
    }

}
