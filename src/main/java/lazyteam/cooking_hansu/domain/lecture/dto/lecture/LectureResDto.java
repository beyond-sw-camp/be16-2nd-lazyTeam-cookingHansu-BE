package lazyteam.cooking_hansu.domain.lecture.dto.lecture;


import lazyteam.cooking_hansu.domain.common.ApprovalStatus;
import lazyteam.cooking_hansu.domain.common.CategoryEnum;
import lazyteam.cooking_hansu.domain.lecture.entity.Lecture;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class LectureResDto {
    private UUID id; // 강의 ID
    private String title; // 강의 제목
    private String description; // 강의 설명
    private CategoryEnum category; // 강의 카테고리
    private ApprovalStatus status; // 강의 상태 (필터링 용도)
    private Integer price; // 강의 가격
    private String thumbUrl;
    private Long reviewCount;
    private Long qnaCount;

    public static LectureResDto fromEntity(Lecture lecture) {
        return LectureResDto.builder()
                .id(lecture.getId())
                .title(lecture.getTitle())
                .description(lecture.getDescription())
                .category(lecture.getCategory())
                .status(lecture.getApprovalStatus())
                .price(lecture.getPrice())
                .thumbUrl(lecture.getThumbUrl())
                .reviewCount(lecture.getReviewCount())
                .qnaCount(lecture.getQnaCount())
                .build();
    }


}
