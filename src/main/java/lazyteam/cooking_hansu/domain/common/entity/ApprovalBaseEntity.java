package lazyteam.cooking_hansu.domain.common.entity;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MappedSuperclass;
import lazyteam.cooking_hansu.domain.common.ApprovalStatus;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 종사자 & 자영업자 승인 관련 공통 필드
 */
@Getter
@MappedSuperclass
public class ApprovalBaseEntity {

    private LocalDateTime approvalTime; // 승인 일자

    @Enumerated(EnumType.STRING)
    private ApprovalStatus approvalStatus; // 승인 상태 (PENDING, APPROVED, REJECTED)

    private LocalDateTime rejectionTime; // 반려 일자

    private String rejectionReason; // 반려 사유
}
