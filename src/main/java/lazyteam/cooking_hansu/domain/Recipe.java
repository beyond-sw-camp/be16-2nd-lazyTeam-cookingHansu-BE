package lazyteam.cooking_hansu.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "recipe")
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recipe_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String thumbnailUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Level level;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Column(name = "cook_time", nullable = false)
    private int cookTime; // 조리 시간 (분)

    // Enum 정의
    public enum Level {
        VERY_HIGH("매우 어려움"),
        HIGH("어려움"),
        MEDIUM("보통"),
        LOW("쉬움"),
        VERY_LOW("매우 쉬움");

        private final String label;

        Level(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    public enum Category {
        KOREAN("한식"),
        CHINESE("중식"),
        WESTERN("양식"),
        JAPANESE("일식");

        private final String label;

        Category(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }
}
