package lazyteam.cooking_hansu.domain.mypage.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder

public class MyLectureListDto {
    private String title;
    private String category;
    private String description;
    private String thumbnailUrl;
    private double averageRating; // 좋아요와 댓글? 은 추후 추가예정
    private Integer studentCount;
}
