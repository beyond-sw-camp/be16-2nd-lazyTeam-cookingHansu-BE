package lazyteam.cooking_hansu.domain.board.entity;

import jakarta.persistence.*;
import lazyteam.cooking_hansu.domain.common.entity.BaseIdAndTimeEntity;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "board")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
public class Board extends BaseIdAndTimeEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId; //유저 아이디

    @Column(nullable = false,length = 255)
    private String title; // 제목

    @Column(columnDefinition = "TEXT")
    private String description; // 설명

    @Column(name = "thumbnail_url", columnDefinition = "TEXT")
    private String thumbnailUrl; // 썸네일 url

    @Builder.Default // 빌더패턴에서 변수 초기화(디폴트 값)시 Builder.Default 어노테이션 필수
    @Column(name = "like_count", nullable = false,columnDefinition = "INT UNSIGNED DEFAULT 0")
    private Integer likeCount = 0;

    @Builder.Default // 빌더패턴에서 변수 초기화(디폴트 값)시 Builder.Default 어노테이션 필수
    @Column(name = "view_count", nullable = false,columnDefinition = "INT UNSIGNED DEFAULT 0")
    private Integer viewCount = 0;

    @Builder.Default // 빌더패턴에서 변수 초기화(디폴트 값)시 Builder.Default 어노테이션 필수
    @Column(name = "bookmark_count", nullable = false,columnDefinition = "INT UNSIGNED DEFAULT 0")
    private Integer bookmarkCount = 0;

    @Builder.Default // 빌더패턴에서 변수 초기화(디폴트 값)시 Builder.Default 어노테이션 필수
    @Column(name = "is_open", nullable = false,columnDefinition = "BOOLEAN DEFAULT true")
    private Boolean isOpen = true;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

//    비즈니스 매서드

//    게시글수정
    public void updateBoard(String title, String description, String thumbnailUrl, Boolean isOpen){
        if (title != null) this.title = title;
        if (description != null) this.description = description;
        if (thumbnailUrl != null) this.thumbnailUrl = thumbnailUrl;
        if (isOpen != null) this.isOpen = isOpen;
    }

//    조회수 증가
    public void incrementViewCount() {this.viewCount++;}

//    좋아요수 증가/감소
    public void incrementLikeCount() {this.likeCount++;}

    public void decrementLikeCount() {
        if (this.likeCount > 0){
            this.likeCount--;
        }
    }

//    북마크 수 중가/감소
    public void incrementBookmarkCount() { this.bookmarkCount++;}

    public void decrementBookmarkCount(){
        if (this.bookmarkCount >0){
            this.bookmarkCount--;
        }
    }

//    레시피 공유 삭제
    public void softDelete() { this.deletedAt = LocalDateTime.now();}

//   삭제 여부 확인
    public boolean isDeleted() { return this.deletedAt != null;}

//   소유자 확인
    public boolean isOwner(Long userId) {return this.userId.equals(userId);}

//   공개 게시글 여부
    public boolean isPublic() { return this.isOpen != null && this.isOpen; }
}
