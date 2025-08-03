package lazyteam.cooking_hansu.domain.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@MappedSuperclass
public class BaseIdAndTimeEntity{
    @Id
    @GeneratedValue
    @Column(name = "id" , updatable = false, nullable = false)
    private UUID id; // UUID 타입의 고유 식별자
    @CreationTimestamp
    private LocalDateTime createdAt; // 생성 시간

    @UpdateTimestamp
    private LocalDateTime updatedAt; // 수정 시간
}
