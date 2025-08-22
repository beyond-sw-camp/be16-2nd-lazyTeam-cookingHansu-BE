package lazyteam.cooking_hansu.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ChatParticipantStatReq {
    private UUID userId; // 사용자 ID
}
