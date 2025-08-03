package lazyteam.cooking_hansu.domain.notice.dto;

import lazyteam.cooking_hansu.domain.admin.entity.Admin;
import lazyteam.cooking_hansu.domain.notice.entity.Notice;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class NoticeListDto {
    private UUID id;
    private String title;
    private LocalDateTime createdAt; // 생성일시
    private String adminName; // 작성자 이름

    public static NoticeListDto fromEntity(Notice notice) {
        return new NoticeListDto(
                notice.getId(),
                notice.getTitle(),
                notice.getCreatedAt(),
                notice.getAdmin().getName()
        );
    }
}
