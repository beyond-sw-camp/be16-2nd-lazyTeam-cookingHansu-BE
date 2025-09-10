package lazyteam.cooking_hansu.domain.user.entity.chef;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonBackReference;
import lazyteam.cooking_hansu.domain.common.entity.BaseIdAndTimeAndApprovalEntity;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
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
public class Chef extends BaseIdAndTimeAndApprovalEntity {

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @MapsId
    // 양방향 관계에서 직렬화 방향 설정 => 순환 참조 해결
    // 직렬화 되지 않도록 수행
    @JsonBackReference
    private User user;

    @Column(nullable = false)
    private String licenseNumber; // 자격 번호

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CuisineType cuisineType; // 자격 업종

    @Column(length = 512, nullable = false)
    private String licenseUrl; // 자격증 이미지 url

    // Chef 정보 업데이트 메서드
    public void updateChefInfo(String licenseNumber, CuisineType cuisineType, String licenseUrl) {
        this.licenseNumber = licenseNumber;
        this.cuisineType = cuisineType;
        this.licenseUrl = licenseUrl;
    }
}
