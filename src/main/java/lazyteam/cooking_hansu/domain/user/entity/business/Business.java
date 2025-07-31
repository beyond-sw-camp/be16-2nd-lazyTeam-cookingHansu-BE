package lazyteam.cooking_hansu.domain.user.entity.business;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MappedSuperclass;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lazyteam.cooking_hansu.domain.user.entity.chef.ApprovalStatus;
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
//@Builder
@MappedSuperclass
public class Business extends User {

    private String businessNumber; // 사업자 등록 번호

    private String businessUrl; // 사업자 등록증 파일 url

    private String businessName; // 상호명

    private String businessAddress; // 사업지 주소

    private String shopCategory; // 사업 업종

    @Enumerated(EnumType.STRING)
    private ApprovalStatus approvalStatus; // 승인 상태 (PENDING, APPROVED, REJECTED)
}
