package lazyteam.cooking_hansu.domain.lecture.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lazyteam.cooking_hansu.domain.admin.entity.Admin;
import lazyteam.cooking_hansu.domain.common.CategoryEnum;
import lazyteam.cooking_hansu.domain.common.LevelEnum;
import lazyteam.cooking_hansu.domain.common.entity.BaseIdAndTimeAndApprovalEntity;
import lazyteam.cooking_hansu.domain.lecture.dto.LectureUpdateDto;
import lazyteam.cooking_hansu.domain.lecture.entity.LectureReview;
import lazyteam.cooking_hansu.domain.lecture.entity.LectureQna;
import lazyteam.cooking_hansu.domain.lecture.entity.LectureVideo;
import lazyteam.cooking_hansu.domain.lecture.entity.LectureIngredientsList;
import lazyteam.cooking_hansu.domain.purchase.entity.PurchasedLecture;
import lazyteam.cooking_hansu.domain.purchase.entity.CartItem;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lombok.*;

import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@Builder


public class Lecture extends BaseIdAndTimeAndApprovalEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_id") // FK 컬럼 이름
    private User submittedBy; // 강의 제출자

    // 승인 관리자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approve_admin_id")
    private Admin approvedBy;

    // 거절 관리자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reject_admin_id")
    private Admin rejectedBy;

    @NotBlank(message = "강의 제목은 필수입니다")
    @Size(max = 100, message = "강의 제목은 100자 이하여야 합니다")
    @Column(nullable = false)
    private String title;

    @Size(max = 1000, message = "강의 설명은 1000자 이하여야 합니다")
    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "강의 난이도는 필수입니다")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LevelEnum level;

    @NotNull(message = "강의 카테고리는 필수입니다")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoryEnum category;

    @NotNull(message = "강의 가격은 필수입니다")
    @Min(value = 0, message = "강의 가격은 0원 이상이어야 합니다")
    @Column(nullable = false)
    private Integer price;

    @Column(columnDefinition = "TEXT")
    private String thumbUrl;


    // 역방향 관계설정(조회용)
    @OneToMany(mappedBy = "lecture", fetch = FetchType.LAZY)

    private List<LectureReview> reviews;

    @OneToMany(mappedBy = "lecture", fetch = FetchType.LAZY)
    private List<LectureQna> qnas;

    @OneToMany(mappedBy = "lecture", fetch = FetchType.LAZY)
    private List<LectureVideo> videos;

    @OneToMany(mappedBy = "lecture", fetch = FetchType.LAZY)
    private List<LectureIngredientsList> ingredientsList;

    @OneToMany(mappedBy = "lecture", fetch = FetchType.LAZY)
    private List<PurchasedLecture> purchases;

    @OneToMany(mappedBy = "lecture", fetch = FetchType.LAZY)
    private List<CartItem> cartItems;


    public void updateImageUrl(String url) {
        this.thumbUrl = url;
    }

    public void updateInfo(LectureUpdateDto dto) {
        if (dto.getTitle() != null) this.title = dto.getTitle();
        if (dto.getDescription() != null) this.description = dto.getDescription();
        if (dto.getCategory() != null) this.category = dto.getCategory();
        if (dto.getCategory() != null) this.level = dto.getLevel();
        if (dto.getPrice() != null) this.price = dto.getPrice();

    }
}
