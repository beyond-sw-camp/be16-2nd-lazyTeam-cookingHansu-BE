package lazyteam.cooking_hansu.domain.common.entity;

import jakarta.persistence.*;
import lazyteam.cooking_hansu.domain.common.ApprovalStatus;
import lombok.Builder;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@MappedSuperclass
public class BaseIdAndTimeAndApprovalEntity{

    @Id
    @GeneratedValue
    @Column(name = "id" , updatable = false, nullable = false)
    private UUID id; // UUID 타입의 고유 식별자

    private LocalDateTime approvalTime; // 승인 일자

    @Enumerated(EnumType.STRING)
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDING; // 승인 상태 (PENDING, APPROVED, REJECTED)

    private LocalDateTime rejectionTime; // 반려 일자

    private String rejectionReason; // 반려 사유

    @CreationTimestamp
    private LocalDateTime createdAt; // 생성 시간

    @UpdateTimestamp
    private LocalDateTime updatedAt; // 수정 시간

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    public void approve(){
        this.approvalStatus = ApprovalStatus.APPROVED;
        this.approvalTime = LocalDateTime.now();
        this.rejectionReason = null; // 승인 시 반려 사유 초기화
        this.rejectionTime = null; // 승인 시 반려 일자 초기화
    }
    public void reject(String reason){
        this.approvalStatus = ApprovalStatus.REJECTED;
        this.rejectionReason = reason;
        this.rejectionTime = LocalDateTime.now();
        this.approvalTime = null; // 반려 시 승인 일자 초기화
    }
}
