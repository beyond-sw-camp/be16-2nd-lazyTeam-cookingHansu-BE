package lazyteam.cooking_hansu.domain.user.controller;

import lazyteam.cooking_hansu.domain.user.dto.HeaderProfileDto;
import lazyteam.cooking_hansu.domain.user.dto.UserLoginDto;
import lazyteam.cooking_hansu.domain.user.service.OAuthServiceFactory;
import lazyteam.cooking_hansu.global.auth.dto.CommonTokenDto;
import lazyteam.cooking_hansu.domain.user.dto.CommonProfileDto;
import lazyteam.cooking_hansu.domain.user.dto.RedirectDto;
import lazyteam.cooking_hansu.domain.user.entity.common.OauthType;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lazyteam.cooking_hansu.domain.user.service.OAuthService;
import lazyteam.cooking_hansu.domain.user.service.UserService;
import lazyteam.cooking_hansu.global.auth.JwtTokenProvider;
import lazyteam.cooking_hansu.global.dto.ResponseDto;
import lazyteam.cooking_hansu.global.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
@Slf4j
public class UserController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final OAuthServiceFactory oAuthServiceFactory;
    private final RefreshTokenService refreshTokenService;

    // 구글 로그인 요청 처리
    @PostMapping("/google/login")
    public ResponseDto<?> googleLogin(@RequestBody RedirectDto redirectDto) {
        return processOAuthLogin(redirectDto.getCode(), OauthType.GOOGLE);
    }

    // 카카오 로그인 요청 처리
    @PostMapping("/kakao/login")
    public ResponseDto<?> kakaoLogin(@RequestBody RedirectDto redirectDto) {
        return processOAuthLogin(redirectDto.getCode(), OauthType.KAKAO);
    }

    /*// 네이버 로그인 요청 처리
    @PostMapping("/naver/login")
    public ResponseDto<?> naverLogin(@RequestBody RedirectDto redirectDto) {
        return processOAuthLogin(redirectDto.getCode(), OauthType.NAVER);
    }*/

    /**
     * OAuth 로그인 공통 처리 메서드
     * @param code 인가 코드
     * @param oauthType OAuth 제공자 타입
     * @return 로그인 응답
     */
    private ResponseDto<?> processOAuthLogin(String code, OauthType oauthType) {
        try {
            // OAuth 서비스 가져오기
            OAuthService oAuthService = oAuthServiceFactory.getOAuthService(oauthType);

            // access token & refresh token 발급
            CommonTokenDto commonTokenDto = oAuthService.getToken(code);

            // 사용자 정보 얻기
            CommonProfileDto commonProfileDto = oAuthService.getProfile(commonTokenDto.getAccess_token());

            // 새 사용자 여부 판단
            User originalUser = userService.getUserBySocialId(commonProfileDto.getSub());
            boolean isNewUser = (originalUser == null);

            // 회원 가입이 되어 있지 않다면 회원가입
            if (isNewUser) {
                originalUser = userService.createOauth(commonProfileDto, oauthType);
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
            log.error("{} login failed", oauthType.name(), e);
            return ResponseDto.fail(HttpStatus.INTERNAL_SERVER_ERROR, "로그인 처리 중 오류가 발생했습니다.");
        }
    }

    // 구글 리프레시 토큰 갱신
    @PostMapping("/google/refresh")
    public ResponseDto<?> googleRefresh(@RequestBody Map<String, String> request) {
        return processTokenRefresh(request);
    }

    // 카카오 리프레시 토큰 갱신
    @PostMapping("/kakao/refresh")
    public ResponseDto<?> kakaoRefresh(@RequestBody Map<String, String> request) {
        return processTokenRefresh(request);
    }

    /*// 네이버 리프레시 토큰 갱신
    @PostMapping("/naver/refresh")
    public ResponseDto<?> naverRefresh(@RequestBody Map<String, String> request) {
        return processTokenRefresh(request);
    }*/

    // 공통 리프레시 토큰 갱신
    @PostMapping("/refresh")
    public ResponseDto<?> refresh(@RequestBody Map<String, String> request) {
        return processTokenRefresh(request);
    }

    /**
     * 토큰 갱신 공통 처리 메서드
     * @param request 리프레시 토큰 요청
     * @return 토큰 갱신 응답
     */
    private ResponseDto<?> processTokenRefresh(Map<String, String> request) {
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

    // 프론트엔드 헤더에 표시될 프로필 정보
    @GetMapping("/profile")
    public ResponseDto<?> getHeaderProfile(@RequestHeader("Authorization") String accessToken) {
        try {
            // access token에서 이메일 추출
            String email = jwtTokenProvider.getEmailFromAccessToken(accessToken);
            if (email == null || email.isEmpty()) {
                return ResponseDto.fail(HttpStatus.UNAUTHORIZED, "Invalid access token");
            }
            // 이메일로 사용자 조회
            User user = userService.getUserByEmail(email);
            if (user == null) {
                return ResponseDto.fail(HttpStatus.BAD_REQUEST, "User not found.");
            }
            // 프로필 정보 응답
            HeaderProfileDto headerProfileDto = HeaderProfileDto.builder()
                    .nickname(user.getNickname())
                    .profileImageUrl(user.getPicture())
                    .build();
            return ResponseDto.ok(headerProfileDto, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Get Header Profile failed: ", e);
            return ResponseDto.fail(HttpStatus.INTERNAL_SERVER_ERROR, "Get Header Profile failed.");
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

    /**
     * 프로필 이미지 업데이트 엔드포인트
     * @param request 프로필 이미지 URL 업데이트 요청
     * @return 업데이트 결과
     */
    @PostMapping("/profile/image")
    public ResponseDto<?> updateProfileImage(@RequestBody Map<String, String> request) {
        try {
            String userId = request.get("userId");
            String newImageUrl = request.get("imageUrl");

            if (userId == null || userId.isEmpty()) {
                return ResponseDto.fail(HttpStatus.BAD_REQUEST, "사용자 ID가 필요합니다.");
            }

            if (newImageUrl == null || newImageUrl.isEmpty()) {
                return ResponseDto.fail(HttpStatus.BAD_REQUEST, "이미지 URL이 필요합니다.");
            }

            // 사용자 조회
            User user = userService.getUserBySocialId(userId);
            if (user == null) {
                return ResponseDto.fail(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다.");
            }

            // 프로필 이미지 업데이트
            String uploadedUrl = userService.updateUserProfileImage(user, newImageUrl);

            Map<String, String> response = new HashMap<>();
            response.put("originalUrl", newImageUrl);
            response.put("uploadedUrl", uploadedUrl);
            response.put("message", "프로필 이미지가 성공적으로 업데이트되었습니다.");

            return ResponseDto.ok(response, HttpStatus.OK);

        } catch (Exception e) {
            log.error("프로필 이미지 업데이트 실패", e);
            return ResponseDto.fail(HttpStatus.INTERNAL_SERVER_ERROR, "프로필 이미지 업데이트 중 오류가 발생했습니다.");
        }
    }
}
