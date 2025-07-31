package lazyteam.cooking_hansu.domain.user.entity.business;

import jakarta.persistence.*;
import lazyteam.cooking_hansu.domain.common.entity.BaseIdAndTimeAndApprovalEntity;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 요식업 사업자 정보 엔티티
 */
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Business extends BaseIdAndTimeAndApprovalEntity {

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @MapsId
    private User user;

    @Column(nullable = false)
    private String businessNumber; // 사업자 등록 번호

    @Column(length = 512, nullable = false)
    private String businessUrl; // 사업자 등록증 파일 url

    @Column(nullable = false)
    private String businessName; // 상호명

    @Column(nullable = false)
    private String businessAddress; // 사업지 주소

    @Column(nullable = false)
    private String shopCategory; // 사업 업종

}
