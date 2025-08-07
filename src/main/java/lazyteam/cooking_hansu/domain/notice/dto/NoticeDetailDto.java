package lazyteam.cooking_hansu.domain.notice.dto;


import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lazyteam.cooking_hansu.domain.notice.entity.Notice;
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
public class NoticeDetailDto {
    private UUID id;
    private String title;
    private String content;
    private String imageUrl; // 이미지 URL을 추가할 수 있습니다.
    private String adminName; // 작성 관리자
    private LocalDateTime createdAt; // 생성일



    public static NoticeDetailDto fromEntity(Notice notice) {
        return NoticeDetailDto.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .imageUrl(notice.getImageUrl())
                .adminName(notice.getAdmin().getName())
                .createdAt(notice.getCreatedAt())
                .build();
    }

}
