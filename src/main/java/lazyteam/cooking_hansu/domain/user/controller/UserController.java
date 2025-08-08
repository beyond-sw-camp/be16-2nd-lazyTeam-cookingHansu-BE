package lazyteam.cooking_hansu.domain.user.controller;

import lazyteam.cooking_hansu.domain.user.dto.UserLoginDto;
import lazyteam.cooking_hansu.global.auth.dto.GoogleTokenDto;
import lazyteam.cooking_hansu.domain.user.dto.GoogleProfileDto;
import lazyteam.cooking_hansu.domain.user.dto.RedirectDto;
import lazyteam.cooking_hansu.domain.user.entity.common.OauthType;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lazyteam.cooking_hansu.domain.user.service.GoogleService;
import lazyteam.cooking_hansu.domain.user.service.UserService;
import lazyteam.cooking_hansu.global.auth.JwtTokenProvider;
import lazyteam.cooking_hansu.global.dto.ResponseDto;
import lazyteam.cooking_hansu.global.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
@Slf4j
public class UserController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final GoogleService googleService;
    private final RefreshTokenService refreshTokenService;

    // 구글 로그인 요청 처리
    @PostMapping("/google/login")
    public ResponseDto<?> googleLogin(@RequestBody RedirectDto redirectDto) {
        try {
            // access token & refresh token 발급
            GoogleTokenDto googleTokenDto = googleService.getToken(redirectDto.getCode());

            // 사용자 정보 얻기
            GoogleProfileDto googleProfileDto = googleService.getGoogleProfile(googleTokenDto.getAccess_token());

            // 새 사용자 여부 판단
            User originalUser = userService.getUserBySocialId(googleProfileDto.getSub());
            boolean isNewUser = (originalUser == null);

            // 회원 가입이 되어 있지 않다면 회원가입
            if (isNewUser) {
                originalUser = userService.createOauth(googleProfileDto, OauthType.GOOGLE);
            }

            // JWT 토큰 발급
            String jwtAtToken = jwtTokenProvider.createAtToken(originalUser);
            String jwtRtToken = jwtTokenProvider.createRtToken(originalUser);

            // Refresh Token을 Redis에 저장
            refreshTokenService.saveRefreshToken(originalUser.getId().toString(), jwtRtToken);

            // 프론트엔드 요구사항에 맞는 응답 데이터 생성
            UserLoginDto userLoginDto = UserLoginDto.builder()
                    .uuid(originalUser.getId())
                    .accessToken(jwtAtToken)
                    .refreshToken(jwtRtToken)
                    .user(originalUser)
                    .isNewUser(isNewUser)
                    .build();

            return ResponseDto.ok(userLoginDto, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Google login failed", e);
            return ResponseDto.fail(HttpStatus.INTERNAL_SERVER_ERROR, "로그인 처리 중 오류가 발생했습니다.");
        }
    }

    // 구글 리프레시 토큰 갱신
    @PostMapping("/google/refresh")
    public ResponseDto<?> googleRefresh(@RequestBody Map<String, String> request) {
        try {
            String refreshToken = request.get("refreshToken");
            if (refreshToken == null || refreshToken.isEmpty()) {
                return ResponseDto.fail(HttpStatus.BAD_REQUEST, "Refresh token is required");
            }

            // 리프레시 토큰을 사용하여 사용자 조회
            User user = refreshTokenService.getRefreshToken(refreshToken);
            if (user == null) {
                return ResponseDto.fail(HttpStatus.UNAUTHORIZED, "Invalid or expired refresh token");
            }

            // 새로운 Access Token과 Refresh Token 발급
            String newAccessToken = jwtTokenProvider.createAtToken(user);
            String newRefreshToken = jwtTokenProvider.createRtToken(user);

            // 새로운 Refresh Token을 Redis에 저장 (기존 토큰 갱신)
            refreshTokenService.updateRefreshToken(user.getId().toString(), newRefreshToken);

            Map<String, String> response = new HashMap<>();
            response.put("accessToken", newAccessToken);
            response.put("refreshToken", newRefreshToken);

            return ResponseDto.ok(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Token refresh failed", e);
            return ResponseDto.fail(HttpStatus.INTERNAL_SERVER_ERROR, "토큰 갱신 중 오류가 발생했습니다.");
        }
    }

    // 로그아웃 (Refresh Token 삭제)
    @PostMapping("/logout")
    public ResponseDto<?> logout(@RequestBody Map<String, String> request) {
        try {
            String refreshToken = request.get("refreshToken");
            if (refreshToken == null || refreshToken.isEmpty()) {
                return ResponseDto.fail(HttpStatus.BAD_REQUEST, "Refresh token is required");
            }

            User user = refreshTokenService.getRefreshToken(refreshToken);
            if (user != null) {
                refreshTokenService.deleteRefreshToken(user.getId().toString());
            }

            return ResponseDto.ok("로그아웃이 완료되었습니다.", HttpStatus.OK);
        } catch (Exception e) {
            log.error("Logout failed", e);
            return ResponseDto.fail(HttpStatus.INTERNAL_SERVER_ERROR, "로그아웃 처리 중 오류가 발생했습니다.");
        }
    }
}
