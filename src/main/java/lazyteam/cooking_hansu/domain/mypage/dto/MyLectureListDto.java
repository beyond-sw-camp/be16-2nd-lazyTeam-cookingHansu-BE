package lazyteam.cooking_hansu.domain.mypage.dto;


import lazyteam.cooking_hansu.domain.common.enums.ApprovalStatus;
import lazyteam.cooking_hansu.domain.common.enums.CategoryEnum;
import lazyteam.cooking_hansu.domain.lecture.entity.Lecture;
import lazyteam.cooking_hansu.domain.purchase.entity.PurchasedLecture;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class MyLectureListDto {
    private UUID id; // 강의 ID
    private String title; // 강의 제목
    private String description; // 강의 설명
    private CategoryEnum category; // 강의 카테고리
    private ApprovalStatus status; // 강의 상태 (필터링 용도)
    private Integer price; // 강의 가격
    private String thumbUrl;
    private Integer reviewCount;
    private Integer qnaCount;
    private Long likeCount;
    private Integer purchaseCount;
    private BigDecimal reviewAvg;

    public static MyLectureListDto fromEntity(PurchasedLecture purchasedLecture) {

        Lecture lecture = purchasedLecture.getLecture();
        int sum = (lecture.getReviewSum()   == null ? 0 : lecture.getReviewSum());
        int cnt = (lecture.getReviewCount() == null ? 0 : lecture.getReviewCount());

//        Decimal 타입의 나눗셈 수행, 0 나눗셈 방지
        BigDecimal avg = (cnt == 0)
                ? BigDecimal.ZERO.setScale(1)
                : BigDecimal.valueOf(sum)
                .divide(BigDecimal.valueOf(cnt), 1, RoundingMode.HALF_UP);

        return MyLectureListDto.builder()
                .id(lecture.getId())
                .title(lecture.getTitle())
                .description(lecture.getDescription())
                .category(lecture.getCategory())
                .status(lecture.getApprovalStatus())
                .price(lecture.getPrice())
                .thumbUrl(lecture.getThumbUrl())
                .reviewCount(lecture.getReviewCount())
                .qnaCount(lecture.getQnaCount())
                .likeCount(lecture.getLikeCount()) // 좋아요 수 추가
                .purchaseCount(lecture.getPurchaseCount())
                .reviewAvg(avg)
                .build();
    }
    // MyLectureListDto.java에 추가할 메서드 (기존 fromEntity 아래에 추가)

    public static MyLectureListDto fromEntity(Lecture lecture) {
        int sum = (lecture.getReviewSum() == null ? 0 : lecture.getReviewSum());
        int cnt = (lecture.getReviewCount() == null ? 0 : lecture.getReviewCount());

        // Decimal 타입의 나눗셈 수행, 0 나눗셈 방지
        BigDecimal avg = (cnt == 0)
                ? BigDecimal.ZERO.setScale(1)
                : BigDecimal.valueOf(sum)
                .divide(BigDecimal.valueOf(cnt), 1, RoundingMode.HALF_UP);

        return MyLectureListDto.builder()
                .id(lecture.getId())
                .title(lecture.getTitle())
                .description(lecture.getDescription())
                .category(lecture.getCategory())
                .status(lecture.getApprovalStatus())
                .price(lecture.getPrice())
                .thumbUrl(lecture.getThumbUrl())
                .reviewCount(lecture.getReviewCount())
                .qnaCount(lecture.getQnaCount())
                .likeCount(lecture.getLikeCount())
                .purchaseCount(lecture.getPurchaseCount())
                .reviewAvg(avg)
                .build();
    }
}
