package com.backend.advert.domain.advertisement.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@JsonInclude(JsonInclude.Include.NON_NULL) // null 값은 직렬화하지 않음
public class QualificationCriteria {

    private Boolean firstTimeParticipation; // 처음 참가 여부
    private Integer minParticipationCount;  // 최소 참가 횟수
    private String excludedAdvertisementId; // 제외해야 할 광고 ID

    public boolean isFirstTimeParticipation() {
        return Boolean.TRUE.equals(this.firstTimeParticipation);
    }
}
