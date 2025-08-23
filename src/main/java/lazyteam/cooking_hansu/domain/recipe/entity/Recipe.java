package lazyteam.cooking_hansu.domain.recipe.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lazyteam.cooking_hansu.domain.common.entity.BaseIdAndTimeEntity;
import lazyteam.cooking_hansu.domain.common.CategoryEnum;
import lazyteam.cooking_hansu.domain.common.LevelEnum;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "recipe")
public class Recipe extends BaseIdAndTimeEntity {

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
    private LevelEnum level;

    @NotNull(message = "레시피 카테고리는 필수입니다")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoryEnum category;

    @NotNull(message = "조리 시간은 필수입니다")
    @Column(name = "cook_time", nullable = false, columnDefinition = "BIGINT UNSIGNED")
    private int cookTime;

    @Min(value = 1, message = "인분 수는 1 이상이어야 합니다")
    @Max(value = 20, message = "인분 수는 20 이하여야 합니다")
    @Column(name = "servings", nullable = true)
    private Integer servings;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Ingredients> ingredients;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecipeStep> steps;

    @Builder
    public Recipe(User user, String description, String title, String thumbnailUrl,
                  LevelEnum level, CategoryEnum category, int cookTime, Integer servings) {
        this.user = user;
        this.description = description;
        this.title = title;
        this.thumbnailUrl = thumbnailUrl;
        this.level = level;
        this.category = category;
        this.cookTime = cookTime;
        this.servings = servings;
    }

    public void updateRecipe(String title, String description, String thumbnailUrl,
                             LevelEnum level, CategoryEnum category, Integer cookTime, Integer servings) {
        if (title != null && !title.trim().isEmpty()) {
            this.title = title.trim();
        }
        if (description != null) {
            this.description = description.trim();
        }
        if (thumbnailUrl != null) {
            this.thumbnailUrl = thumbnailUrl.trim();
        }
        if (level != null) {
            this.level = level;
        }
        if (category != null) {
            this.category = category;
        }
        if (cookTime != null && cookTime > 0) {
            this.cookTime = cookTime;
        }
        if (servings != null && servings > 0 && servings <= 20) {
            this.servings = servings;
        }
    }

    public boolean isOwnedBy(User user) {
        return this.user != null && this.user.getId().equals(user.getId());
    }

//    public String getServingsText() {
//        return this.servings != null ? this.servings + "인분" : null;
//    }
}