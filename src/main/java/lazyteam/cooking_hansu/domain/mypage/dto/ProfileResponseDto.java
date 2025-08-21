package lazyteam.cooking_hansu.domain.mypage.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ProfileResponseDto {
    private String nickname;
    private String email;
    private String info;
    private String profileImageUrl;
    private String userType;
}
