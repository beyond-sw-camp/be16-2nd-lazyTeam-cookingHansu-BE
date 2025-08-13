package lazyteam.cooking_hansu.domain.lecture.dto.lecture;

import lazyteam.cooking_hansu.domain.common.CategoryEnum;
import lazyteam.cooking_hansu.domain.common.LevelEnum;
import lazyteam.cooking_hansu.domain.lecture.entity.*;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder

public class LectureDetailDto {

    private UUID lectureId;
    private String nickname; // 강의 제출자 닉네임
    private String name; // 강의 제출자 이름
    private String title;
    private String description;
    private LevelEnum level;
    private CategoryEnum category;
    private Integer price;
    private long reviewCount = 0L;
    private long qnaCount = 0L;

//    재료 목록
    private List<LectureIngredResDto> ingredResDtoList;

//    조리 순서
    private List<LectureStepResDto> lectureStepResDtoList;

//    강의 QnA
    private List<QnaResDto> qnaList;

//    강의 영상
    private List<LectureVideoResDto> lectureVideoResDtoList;

//    강의 리뷰
    private List<LectureReviewResDto> lectureReviewResDtoList;


    public static LectureDetailDto fromEntity(Lecture lecture, User submittedBy, List<LectureReview> reviews
            , List<LectureQna> qnas, List<LectureVideo> videos, List<LectureIngredientsList> ingredientsList
            ,List<LectureStep> lectureStepList) {

        return LectureDetailDto.builder()
                .lectureId(lecture.getId())
                .nickname(submittedBy.getNickname()) // 강의제출자
                .name(submittedBy.getName()) //강의 제출자
                .title(lecture.getTitle())
                .description(lecture.getDescription())
                .level(lecture.getLevel())
                .category(lecture.getCategory())
                .price(lecture.getPrice())
                .reviewCount(lecture.getReviewCount())
                .qnaCount(lecture.getQnaCount())

                .ingredResDtoList(ingredientsList.stream().map(LectureIngredResDto::fromEntity).toList())

                .lectureStepResDtoList(lectureStepList.stream().map(LectureStepResDto::fromEntity).toList())

                .qnaList(qnas.stream().map(QnaResDto::fromEntity).toList())

                .lectureVideoResDtoList(videos.stream().map(LectureVideoResDto::fromEntity).toList())

                .lectureReviewResDtoList(reviews.stream().map(LectureReviewResDto::fromEntity).toList())
                .build();
    }

}
