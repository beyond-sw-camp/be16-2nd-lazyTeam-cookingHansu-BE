package lazyteam.cooking_hansu.domain.admin.dto;

import lazyteam.cooking_hansu.domain.admin.entity.Admin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 관리자 로그인 전용 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminLoginDto {

    private UUID adminId;
    private String adminName;
    private String adminEmail;
    private String accessToken;
    private String refreshToken;
    private Long expiresIn;

    public static AdminLoginDto fromEntity(Admin admin, String accessToken, String refreshToken, Long expiresIn) {
        return AdminLoginDto.builder()
                .adminId(admin.getId())
                .adminName(admin.getName())
                .adminEmail(admin.getEmail())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(expiresIn)
                .build();
    }
}
