package lazyteam.cooking_hansu.domain.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ChatRoomUpdateDto {
    
    @NotBlank(message = "채팅방 이름은 필수입니다")
    @Size(max = 100, message = "채팅방 이름은 100자 이하여야 합니다")
    private String name; // 변경할 채팅방 이름
} 