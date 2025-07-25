package lazyteam.cooking_hansu.domain.lecture.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lazyteam.cooking_hansu.domain.common.CategoryEnum;
import lazyteam.cooking_hansu.domain.common.LevelEnum;
import lazyteam.cooking_hansu.domain.common.StatusEnum;
import lombok.*;

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

    //fk설정 필요(조회용)
//    요청자 컬럼ERD 바꾸고 관계성 설정 다시
    @JoinColumn(name = "administrator_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Long submittedId;

//    @JoinColumn(name = "administrator_id")
//    @ManyToOne(fetch = FetchType.LAZY)
    private Long approveAdminId;

//    @JoinColumn(name = "administrator_id")
//    @ManyToOne(fetch = FetchType.LAZY)
    private Long rejectAdminId;

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

}
