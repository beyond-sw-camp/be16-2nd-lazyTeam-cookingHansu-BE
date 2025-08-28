package lazyteam.cooking_hansu.domain.lecture.dto.lecture;

import lazyteam.cooking_hansu.domain.common.enums.CategoryEnum;
import lazyteam.cooking_hansu.domain.common.enums.LevelEnum;
import lazyteam.cooking_hansu.domain.lecture.entity.*;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
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
    private String submittedByProfile; // 강의 제출자 프로필
    private String title;
    private String description;
    private LevelEnum level;
    private CategoryEnum category;
    private Integer price;
    private Integer reviewCount;
    private Integer qnaCount;
    private Long likeCount; // 좋아요 수 추가
    private Integer purchaseCount;
    private BigDecimal reviewAvg;
    private UUID submittedById;

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

        int sum = (lecture.getReviewSum()   == null ? 0 : lecture.getReviewSum());
        int cnt = (lecture.getReviewCount() == null ? 0 : lecture.getReviewCount());

//        Decimal 타입의 나눗셈 수행, 0 나눗셈 방지
        BigDecimal avg = (cnt == 0)
                ? BigDecimal.ZERO.setScale(1)
                : BigDecimal.valueOf(sum)
                .divide(BigDecimal.valueOf(cnt), 1, RoundingMode.HALF_UP);

        return LectureDetailDto.builder()
                .lectureId(lecture.getId())
                .nickname(submittedBy.getNickname()) // 강의제출자
                .name(submittedBy.getName()) //강의 제출자
                .submittedByProfile(submittedBy.getPicture()) // 강의 제출자 프로필
                .title(lecture.getTitle())
                .description(lecture.getDescription())
                .level(lecture.getLevel())
                .category(lecture.getCategory())
                .price(lecture.getPrice())
                .reviewCount(lecture.getReviewCount())
                .qnaCount(lecture.getQnaCount())
                .likeCount(lecture.getLikeCount()) // 좋아요 수 추가
                .purchaseCount(lecture.getPurchaseCount())
                .reviewAvg(avg)
                .submittedById(submittedBy.getId())
                .ingredResDtoList(ingredientsList.stream().map(LectureIngredResDto::fromEntity).toList())

                .lectureStepResDtoList(
                        lectureStepList.stream()
                                .sorted(Comparator.comparingInt(LectureStep::getStepSequence))
                                .map(LectureStepResDto::fromEntity)
                                .toList()
                )

                .qnaList(
                        qnas.stream()
                                .filter(q -> q.getParent() == null)  //부모만
                                .map(QnaResDto::fromEntity)
                                .toList()
                )

                .lectureVideoResDtoList(
                        videos.stream()
                                .sorted(Comparator.comparingInt(v -> v.getSequence()))
                                .map(LectureVideoResDto::fromEntity)
                                .toList()
                )

                .lectureReviewResDtoList(reviews.stream().map(LectureReviewResDto::fromEntity).toList())
                .build();
    }

}
