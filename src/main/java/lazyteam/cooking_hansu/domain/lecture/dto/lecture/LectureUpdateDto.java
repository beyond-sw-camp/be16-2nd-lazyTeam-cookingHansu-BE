package lazyteam.cooking_hansu.domain.lecture.dto.lecture;

import lazyteam.cooking_hansu.domain.common.enums.CategoryEnum;
import lazyteam.cooking_hansu.domain.common.enums.LevelEnum;
import lazyteam.cooking_hansu.domain.lecture.entity.Lecture;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder

public class LectureUpdateDto {

    private User user;

    private String title;

    private String description;

    private LevelEnum level;

    private CategoryEnum category;

    private Integer price;

    public Lecture toEntity(User user) {
        return Lecture.builder()
                .submittedBy(user)
                .title(title)
                .description(description)
                .level(level)
                .category(category)
                .price(price)
                .build();
    }
}
