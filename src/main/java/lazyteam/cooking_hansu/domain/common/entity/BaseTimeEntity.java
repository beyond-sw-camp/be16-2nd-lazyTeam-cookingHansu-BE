package lazyteam.cooking_hansu.domain.common.entity;


import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass
public class BaseTimeEntity {
    @CreationTimestamp
    private LocalDateTime createdAt; // 생성 시간

    @UpdateTimestamp
    private LocalDateTime updatedAt; // 수정 시간

    private LocalDateTime deletedAt; // 삭제 시간
}
