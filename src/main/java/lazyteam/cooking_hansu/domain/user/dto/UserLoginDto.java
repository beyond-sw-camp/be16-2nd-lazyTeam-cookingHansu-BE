package lazyteam.cooking_hansu.domain.user.dto;

import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLoginDto {
    private UUID uuid;
    private String accessToken;
    private String refreshToken;
    private Long expiresIn;
    private User user;

    public static UserLoginDto fromEntity(User user, String accessToken, String refreshToken, Long expiresIn) {
        return UserLoginDto.builder()
                .uuid(user.getId())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(expiresIn)
                .user(user)
                .build();
    }

    public boolean isNewUser() {
        return user != null ? user.isNewUser() : false;
    }
}
