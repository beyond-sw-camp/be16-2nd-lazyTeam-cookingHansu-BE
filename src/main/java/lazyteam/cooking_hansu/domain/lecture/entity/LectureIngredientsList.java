package lazyteam.cooking_hansu.domain.lecture.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@Builder
public class LectureIngredientsList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long lectureIngredientsId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id", nullable = false)
    private Lecture lecture;

    @NotNull
    private String IngredientsName;

    @NotNull
    private String amount;

}
