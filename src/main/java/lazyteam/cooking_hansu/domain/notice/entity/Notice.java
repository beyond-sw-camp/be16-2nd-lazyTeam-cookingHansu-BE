package lazyteam.cooking_hansu.domain.notice.entity;

import lazyteam.cooking_hansu.domain.common.entity.BaseTimeEntity;
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
public class Notice extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 공지사항 ID

    @NotNull(message = "제목은 필수 입력입니다.")
    @Column(nullable = false, length = 100)
    private String title; // 공지사항 제목
    @NotNull(message = "내용은 필수 입력입니다.")
    @Column(nullable = false, length = 3000)
    private String content; // 공지사항 내용

    private String imageUrl; // 공지사항 이미지 URL

    private LocalDateTime deletedAt; // 삭제 시간

    private String writer; // 작성자 (관리자)

    public void updateNotice(NoticeDetailDto noticeDetailDto) {
        this.title = noticeDetailDto.getTitle();
        this.content = noticeDetailDto.getContent();
        this.imageUrl = noticeDetailDto.getImageUrl();
        this.writer = "관리자"; // TODO: 작성자는 현재 하드코딩되어 있지만, 실제로는 인증된 사용자 정보를 가져와야 함.
    }
}
