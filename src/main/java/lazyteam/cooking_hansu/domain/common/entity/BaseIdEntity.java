package lazyteam.cooking_hansu.domain.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

import java.util.UUID;

@Getter
@MappedSuperclass
public class BaseIdEntity {
    @Id
    @GeneratedValue
    @Column(name = "id" , updatable = false, nullable = false)
    private UUID id; // UUID 타입의 고유 식별자
}
