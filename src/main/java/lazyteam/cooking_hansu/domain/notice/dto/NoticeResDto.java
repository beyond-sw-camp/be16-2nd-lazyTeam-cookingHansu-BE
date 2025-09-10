package lazyteam.cooking_hansu.domain.notice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class NoticeResDto {
    private UUID id;
    private String title;
    private String content;
    private String imageUrl;
    private LocalDateTime createdAt;
}
