package lazyteam.cooking_hansu.domain.user.entity.chef;

import jakarta.persistence.*;
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
public class Chef {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @MapsId
    private User user;

    @Column(nullable = false)
    private String licenseNumber; // 자격 번호

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CuisineType cuisineType; // 자격 업종

    @Column(length = 512, nullable = false)
    private String licenseUrl; // 자격증 이미지 url

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApprovalStatus approvalStatus; // 승인 상태

}
