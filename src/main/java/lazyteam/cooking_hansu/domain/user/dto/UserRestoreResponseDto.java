package lazyteam.cooking_hansu.domain.user.dto;

import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRestoreResponseDto {
    private String userId;
    private String email;
    private String name;
    private String nickname;
    private String picture;
    private String message;
    private String accessToken;
    private String refreshToken;
    private Long expiresIn;

    public static UserRestoreResponseDto fromEntity(User user, String accessToken, String refreshToken, Long expiresIn) {
        return UserRestoreResponseDto.builder()
                .userId(user.getId().toString())
                .email(user.getEmail())
                .name(user.getName())
                .nickname(user.getNickname())
                .picture(user.getPicture())
                .message("회원 복구가 성공적으로 완료되었습니다.")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(expiresIn)
                .build();
    }
}
