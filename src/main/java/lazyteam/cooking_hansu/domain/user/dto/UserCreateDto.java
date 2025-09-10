package lazyteam.cooking_hansu.domain.user.dto;

import lazyteam.cooking_hansu.domain.user.entity.common.OauthType;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// 사용자 생성 DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreateDto {

    private String email;
    private String name;
    private OauthType oauthType;
    private String socialId;
    private String picture;

    public static User toEntity(String sub, String name, String email, String picture, OauthType oauthType) {
        return User.builder()
                .email(email)
                .name(name)
                .oauthType(oauthType)
                .socialId(sub)
                .picture(picture)
                .build();
    }
}