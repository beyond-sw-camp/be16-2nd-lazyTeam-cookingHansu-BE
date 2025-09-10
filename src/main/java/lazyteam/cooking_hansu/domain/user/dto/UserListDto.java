package lazyteam.cooking_hansu.domain.user.dto;

import lazyteam.cooking_hansu.domain.user.entity.common.LoginStatus;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
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
public class UserListDto {
    private UUID userId; // 사용자 ID
    private String nickname; // 사용자 닉네임
    private String email; // 사용자 이메일
    private String picture; // 프로필 이미지 URL
    private LocalDateTime createdAt; // 생성일시
    private LoginStatus loginStatus; // 로그인 상태

    public static UserListDto fromEntity(User user) {
        return UserListDto.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .picture(user.getPicture())
                .createdAt(user.getCreatedAt())
                .loginStatus(user.getLoginStatus())
                .build();
    }
}
