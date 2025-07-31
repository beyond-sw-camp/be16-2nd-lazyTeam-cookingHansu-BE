package lazyteam.cooking_hansu.domain.recipe.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lazyteam.cooking_hansu.domain.common.entity.BaseIdEntity;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "recipe")
public class Recipe extends BaseIdEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Size(max = 1000, message = "레시피 설명은 1000자 이하여야 합니다")
    @Column(columnDefinition = "TEXT")
    private String description;

    @NotBlank(message = "레시피 제목은 필수입니다")
    @Size(max = 255, message = "레시피 제목은 255자 이하여야 합니다")
    @Column(nullable = false, length = 255)
    private String title;

    @Size(max = 512, message = "썸네일 URL은 512자 이하여야 합니다")
    @Column(columnDefinition = "TEXT")
    private String thumbnailUrl;

    @NotNull(message = "레시피 난이도는 필수입니다")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LevelType level;

    @NotNull(message = "레시피 카테고리는 필수입니다")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoryType category;

    @NotNull(message = "조리 시간은 필수입니다")
    @Column(name = "cook_time", nullable = false, columnDefinition = "BIGINT UNSIGNED")
    private int cookTime; // 조리 시간 (분)

}
