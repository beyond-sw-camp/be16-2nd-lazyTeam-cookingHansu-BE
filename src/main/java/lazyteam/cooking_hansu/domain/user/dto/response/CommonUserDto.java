package lazyteam.cooking_hansu.domain.user.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lazyteam.cooking_hansu.domain.user.entity.common.GeneralType;
import lazyteam.cooking_hansu.domain.user.entity.common.Role;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lombok.*;

import java.util.UUID;

@AllArgsConstructor
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@ToString @EqualsAndHashCode
public class CommonUserDto {

    private UUID id;
    @Email(message = "유효한 이메일 형식이 아닙니다")
    @NotBlank(message = "이메일은 필수입니다")
    private String email;
    @Size(message = "이름은 2자 이상 50자 이하여야 합니다", min = 2, max = 50)
    private String name;
    @Size(message = "프로필 이미지 URL은 512자 이하여야 합니다", max = 512)
    private String picture;
    @Size(message = "닉네임은 2자 이상 20자 이하여야 합니다", min = 2, max = 20)
    private String nickname;
    @Size(max = 200, message = "자기소개는 200자 이하여야 합니다")
    private String info;
    private Role role;
    private GeneralType generalType;
    private ChefDto chef;
    private BusinessDto business;

    public static CommonUserDto fromEntity(User user) {
        return CommonUserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .picture(user.getPicture())
                .nickname(user.getNickname())
                .info(user.getInfo())
                .role(user.getRole())
                .generalType(user.getGeneralType())
                .chef(user.getChef() != null ? ChefDto.fromEntity(user.getChef()) : null)
                .business(user.getOwner() != null ? BusinessDto.fromEntity(user.getOwner()) : null)
                .build();
    }
}