package lazyteam.cooking_hansu.domain.lecture.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lazyteam.cooking_hansu.domain.admin.entity.Admin;
import lazyteam.cooking_hansu.domain.common.CategoryEnum;
import lazyteam.cooking_hansu.domain.common.LevelEnum;
import lazyteam.cooking_hansu.domain.common.StatusEnum;
import lazyteam.cooking_hansu.domain.purchase.entity.CartItem;
import lazyteam.cooking_hansu.domain.purchase.entity.PurchasedLecture;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lombok.*;

import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@Builder

public class Lecture {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long lectureId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_id") // FK 컬럼 이름
    private User user;

    // 승인 관리자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approve_admin_id")
    private Admin approveAdminId;

    // 거절 관리자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reject_admin_id")
    private Admin rejectAdminId;

    @NotNull
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull
    private LevelEnum level;

    @NotNull
    private CategoryEnum category;

    @NotNull
    private Integer price;

    @Column(columnDefinition = "TEXT")
    private String thumbUrl;

    @NotNull
    private StatusEnum status;

    private String approvedAt;

    private String rejectedAt;

    @Column(columnDefinition = "TEXT")
    private String reject_reason;

    private String createdAt;

    private String updatedAt;


////    역방향 관계설정(조회용)
//
//    @OneToMany(mappedBy = "lecture")
//    private List<LectureReview> reviews;
//
//    @OneToMany(mappedBy = "lecture")
//    private List<LectureQna> qnas;
//
//    @OneToMany(mappedBy = "lecture")
//    private List<LectureVideo> videos;
//
//    @OneToMany(mappedBy = "lecture")
//    private List<LectureIngredientsList> ingredientsList;
//
//    @OneToMany(mappedBy = "lecture")
//    private List<PurchasedLecture> purchases;
//
//    @OneToMany(mappedBy = "lecture")
//    private List<CartItem> cartItems;

}
