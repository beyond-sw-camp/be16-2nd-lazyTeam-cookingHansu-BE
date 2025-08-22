package lazyteam.cooking_hansu.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ChatInviteReqDto {
    private UUID myId; // 내 ID
    private UUID inviteeId; // 초대할 참여자의 ID
}
