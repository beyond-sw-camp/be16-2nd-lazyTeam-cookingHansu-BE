package lazyteam.cooking_hansu.domain.notice.entity;

import lazyteam.cooking_hansu.domain.admin.entity.Admin;
import lazyteam.cooking_hansu.domain.common.entity.BaseIdAndTimeEntity;
import lazyteam.cooking_hansu.domain.notice.dto.NoticeDetailDto;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Entity
@Builder
public class Notice extends BaseIdAndTimeEntity {

    // 공지사항 제목
    @NotNull(message = "제목은 필수 입력입니다.")
    @Column(nullable = false, length = 100)
    private String title;

    // 공지사항 내용
    @NotNull(message = "내용은 필수 입력입니다.")
    @Column(nullable = false, length = 3000)
    private String content;

    // 공지사항 이미지 URL
    private String imageUrl;

    // 삭제 시간
    private LocalDateTime deletedAt;

    // 관리자 ID (FK)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "administrator_id", nullable = false)
    private Admin admin;

    public void updateNotice(NoticeDetailDto noticeDetailDto, Admin admin) {
        this.title = noticeDetailDto.getTitle();
        this.content = noticeDetailDto.getContent();
        this.imageUrl = noticeDetailDto.getImageUrl();
        this.admin = admin;
    }
}
