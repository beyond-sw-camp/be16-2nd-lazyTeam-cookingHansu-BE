package lazyteam.cooking_hansu.domain.user.controller;

import lazyteam.cooking_hansu.domain.user.dto.response.CommonUserDto;
import lazyteam.cooking_hansu.domain.user.dto.response.HeaderProfileDto;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lazyteam.cooking_hansu.domain.user.service.UserService;
import lazyteam.cooking_hansu.global.dto.ResponseDto;
import lazyteam.cooking_hansu.global.auth.JwtTokenProvider;
import lazyteam.cooking_hansu.global.dto.ResponseDto;
import lazyteam.cooking_hansu.global.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
@Slf4j
public class UserController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    // 리프레시 토큰 갱신
    @PostMapping("/refresh")
    public ResponseDto<?> updateRefreshToken(@RequestBody Map<String, String> request) {
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

            // 프론트엔드 요구사항에 맞는 응답 구조
            Map<String, Object> response = new HashMap<>();
            response.put("accessToken", newAccessToken);
            response.put("refreshToken", newRefreshToken);
            // expiresIn은 밀리초 단위로 제공 (프론트엔드에서 Date.now() + expiresIn 형태로 사용)
            response.put("expiresIn", 3600 * 1000L); // 1시간을 밀리초로 변환

            log.info("Token refresh successful for user: {}", user.getEmail());
            return ResponseDto.ok(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Token refresh failed", e);
            return ResponseDto.fail(HttpStatus.INTERNAL_SERVER_ERROR, "토큰 갱신 중 오류가 발생했습니다.");
        }
    }

    // TODO: 회원 관련 API 메서드 구현 예정

    // 회원 탈퇴
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteUser() {
        userService.deleteUser();
        return new ResponseEntity<>(
                ResponseDto.ok("회원 탈퇴가 완료되었습니다.", HttpStatus.OK),
                HttpStatus.OK
        );
    }
    // 프론트엔드 헤더에 표시될 프로필 정보
    @GetMapping("/profile")
    public ResponseDto<?> getHeaderProfile(@RequestHeader("Authorization") String accessToken) {
        try {
            // Authorization 헤더에서 토큰 추출 (Bearer 접두사 제거)
            if (accessToken.startsWith("Bearer ")) {
                accessToken = accessToken.substring(7);
            }

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
            log.info("User profile found: {}", headerProfileDto);
            return ResponseDto.ok(headerProfileDto, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Get Header Profile failed: ", e);
            return ResponseDto.fail(HttpStatus.INTERNAL_SERVER_ERROR, "Get Header Profile failed.");
        }
    }

    /**
     * 로그인한 사용자 정보 조회
     */
    @GetMapping("/me")
    public ResponseDto<?> getCurrentUser(@RequestHeader("Authorization") String accessToken) {
        try {
            // Authorization 헤더에서 토큰 추출 ("Bearer " 제거)
            if (accessToken.startsWith("Bearer ")) {
                accessToken = accessToken.substring(7);
            }

            // Access Token에서 이메일 추출
            String email = jwtTokenProvider.getEmailFromAccessToken(accessToken);
            if (email == null || email.isEmpty()) {
                return ResponseDto.fail(HttpStatus.UNAUTHORIZED, "유효하지 않은 Access Token 입니다.");
            }

            // 이메일로 사용자 조회
            User user = userService.getUserByEmail(email);
            if (user == null) {
                return ResponseDto.fail(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다.");
            }

            // 사용자 정보 조회
            CommonUserDto userDto = CommonUserDto.fromEntity(user);
            log.info("Current user info retrieved: {}", userDto);

            return ResponseDto.ok(userDto, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Get current user failed : ", e);
            return ResponseDto.fail(HttpStatus.INTERNAL_SERVER_ERROR, "Get current user failed.");
        }
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseDto<?> logout(@RequestHeader("Authorization") String accessTokenHeader) {
        try {
            // Authorization 헤더에서 토큰 추출 (Bearer 접두사 제거)
            String accessToken = accessTokenHeader;
            if (accessToken.startsWith("Bearer ")) {
                accessToken = accessToken.substring(7);
            }

            // Access Token에서 이메일 추출
            String email = jwtTokenProvider.getEmailFromAccessToken(accessToken);
            if (email == null || email.isEmpty()) {
                return ResponseDto.fail(HttpStatus.UNAUTHORIZED, "유효하지 않은 Access Token입니다.");
            }

            // 이메일로 사용자 조회
            User user = userService.getUserByEmail(email);
            if (user == null) {
                return ResponseDto.fail(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다.");
            }

            // Redis에서 해당 사용자의 Refresh Token 삭제
            refreshTokenService.deleteRefreshToken(user.getId().toString());
            log.info("User logged out successfully: {}", email);

            Map<String, String> response = new HashMap<>();
            response.put("message", "로그아웃이 성공적으로 처리되었습니다.");

            return ResponseDto.ok(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Logout failed", e);
            return ResponseDto.fail(HttpStatus.INTERNAL_SERVER_ERROR, "로그아웃 처리 중 오류가 발생했습니다.");
        }
    }

}
