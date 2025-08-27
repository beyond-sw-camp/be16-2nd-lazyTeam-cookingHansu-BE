package lazyteam.cooking_hansu.domain.purchase.dto;

import lazyteam.cooking_hansu.domain.common.enums.ApprovalStatus;
import lazyteam.cooking_hansu.domain.common.enums.CategoryEnum;
import lazyteam.cooking_hansu.domain.common.enums.PayMethod;
import lazyteam.cooking_hansu.domain.lecture.entity.Lecture;
import lazyteam.cooking_hansu.domain.purchase.entity.Payment;
import lazyteam.cooking_hansu.domain.purchase.entity.PurchasedLecture;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class PurchasedLectureHistoryDto {
    private UUID id; // 강의 ID
    private String title; // 강의 제목
    private Integer price; // 강의 가격
    private String thumbUrl; // 강의 썸네일

//    payment 엔티티
    private LocalDateTime createdAt; // 결제 시각
    private String orderId; // 주문 번호
    private PayMethod payMethod; // 결제 방법
    private String buyerName; // 구매자 이름
    private String buyerEmail; // 구매자 이메일

    public static PurchasedLectureHistoryDto fromEntity(PurchasedLecture purchasedLecture) {

        Lecture lecture = purchasedLecture.getLecture();
        Payment payment = purchasedLecture.getPayment();
        return PurchasedLectureHistoryDto.builder()
                .id(purchasedLecture.getId())
                .title(purchasedLecture.getLectureTitleSnapshot())
                .price(lecture.getPrice())
                .thumbUrl(purchasedLecture.getLecture().getThumbUrl())
                .createdAt(payment.getCreatedAt())
                .orderId(payment.getOrderId())
                .payMethod(payment.getPayMethod())
                .buyerName(payment.getUser().getName())
                .buyerEmail(payment.getUser().getEmail())
                .build();
    }
}
