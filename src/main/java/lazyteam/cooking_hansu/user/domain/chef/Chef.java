package lazyteam.cooking_hansu.user.domain.chef;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MappedSuperclass;
import lazyteam.cooking_hansu.user.domain.common.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 요식업 종사자 정보 엔티티
 */
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@MappedSuperclass
public class Chef extends User {

    private String licenseNumber; // 자격 번호

    @Enumerated(EnumType.STRING)
    private CuisineType cuisineType; // 자격 업종

    private String licenseUrl; // 자격증 이미지 url

    @Enumerated(EnumType.STRING)
    private ApprovalStatus approvalStatus; // 승인 상태

}
