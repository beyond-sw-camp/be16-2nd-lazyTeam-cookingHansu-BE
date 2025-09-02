package lazyteam.cooking_hansu.domain.user.controller;

import jakarta.persistence.EntityNotFoundException;
import lazyteam.cooking_hansu.domain.user.dto.UserRestoreRequestDto;
import lazyteam.cooking_hansu.domain.user.dto.UserRestoreResponseDto;
import lazyteam.cooking_hansu.domain.user.dto.response.CommonUserDto;
import lazyteam.cooking_hansu.domain.user.dto.response.HeaderProfileDto;
import lazyteam.cooking_hansu.domain.user.entity.common.OauthType;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lazyteam.cooking_hansu.domain.user.service.UserService;
import lazyteam.cooking_hansu.global.dto.ResponseDto;
import lazyteam.cooking_hansu.global.auth.JwtTokenProvider;
import lazyteam.cooking_hansu.global.service.RefreshTokenService;
import lazyteam.cooking_hansu.global.service.S3Uploader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
@Slf4j
public class UserController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final S3Uploader s3Uploader;

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

    // 프론트엔드 헤더에 표시될 프로필 정보
    @GetMapping("/profile")
    public ResponseDto<?> getHeaderProfile(@RequestHeader("Authorization") String accessToken) {
        try {
            // Authorization 헤더에서 토큰 추출 (Bearer 접두사 제거)
            if (accessToken.startsWith("Bearer ")) {
                accessToken = accessToken.substring(7);
            }

            // access token에서 이메일 추출
            String id = jwtTokenProvider.getIdFromAccessToken(accessToken);
            if (id == null || id.isEmpty()) {
                return ResponseDto.fail(HttpStatus.UNAUTHORIZED, "Invalid access token");
            }
            // 이메일로 사용자 조회
            User user = userService.getUserById(UUID.fromString(id));
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
            String id = jwtTokenProvider.getIdFromAccessToken(accessToken);
            if (id == null || id.isEmpty()) {
                return ResponseDto.fail(HttpStatus.UNAUTHORIZED, "유효하지 않은 Access Token 입니다.");
            }

            // 이메일로 사용자 조회
            User user = userService.getUserById(UUID.fromString(id));
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
            String id = jwtTokenProvider.getIdFromAccessToken(accessToken);
            if (id == null || id.isEmpty()) {
                return ResponseDto.fail(HttpStatus.UNAUTHORIZED, "유효하지 않은 Access Token입니다.");
            }

            // 이메일로 사용자 조회
            User user = userService.getUserById(UUID.fromString(id));
            if (user == null) {
                return ResponseDto.fail(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다.");
            }

            // Redis에서 해당 사용자의 Refresh Token 삭제
            refreshTokenService.deleteRefreshToken(user.getId().toString());
            log.info("User logged out successfully: {}", id);

            Map<String, String> response = new HashMap<>();
            response.put("message", "로그아웃이 성공적으로 처리되었습니다.");

            return ResponseDto.ok(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Logout failed", e);
            return ResponseDto.fail(HttpStatus.INTERNAL_SERVER_ERROR, "로그아웃 처리 중 오류가 발생했습니다.");
        }
    }


    /**
     * 회원 탈퇴
     */
    @DeleteMapping("/delete")
    public ResponseDto<?> deleteUser(@RequestHeader("Authorization") String accessToken) {
        try {
            if (accessToken.startsWith("Bearer ")) {
                accessToken = accessToken.substring(7);
            }

            String id = jwtTokenProvider.getIdFromAccessToken(accessToken);
            if (id == null || id.isEmpty()) {
                return ResponseDto.fail(HttpStatus.UNAUTHORIZED, "유효하지 않은 Access Token 입니다.");
            }

            userService.deleteUser(UUID.fromString(id));

            Map<String, String> response = new ConcurrentHashMap<>();
            response.put("message", "회원 탈퇴가 성공적으로 처리되었습니다.");

            return ResponseDto.ok(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Delete user failed", e);
            return ResponseDto.fail(HttpStatus.INTERNAL_SERVER_ERROR, "회원 탈퇴 처리 중 오류가 발생했습니다.");
        }
    }

    /**
     * 회원 복구
     */
    @PostMapping("/restore")
    public ResponseDto<?> restoreUser(@RequestBody UserRestoreRequestDto requestDto) {
        try {
            // OAuth 타입 변환
            OauthType oauthType = OauthType.valueOf(requestDto.getOauthType().toUpperCase());

            // 새로운 프로필 이미지 처리 (S3 업로드)
            String uploadedPictureUrl = null;
            if (requestDto.getPicture() != null && !requestDto.getPicture().isEmpty()) {
                try {
                    String fileName = requestDto.getSocialId(); // 소셜 ID를 파일명으로 사용
                    uploadedPictureUrl = s3Uploader.uploadFromUrl(
                            requestDto.getPicture(),
                            "profiles/",
                            fileName
                    );
                    log.info("복구 시 프로필 이미지 S3 업로드 성공: {} -> {}", requestDto.getPicture(), uploadedPictureUrl);
                } catch (Exception e) {
                    log.warn("복구 시 프로필 이미지 S3 업로드 실패, 원본 URL 사용: {}", requestDto.getPicture(), e);
                    uploadedPictureUrl = requestDto.getPicture(); // 업로드 실패 시 원본 URL 사용
                }
            }

            // 회원 복구
            User restoredUser = userService.restoreUser(requestDto.getSocialId(), oauthType, uploadedPictureUrl);

            // JWT 토큰 발급
            String jwtAtToken = jwtTokenProvider.createAtToken(restoredUser);
            String jwtRtToken = jwtTokenProvider.createRtToken(restoredUser);
            Long expiresIn = jwtTokenProvider.getRefreshTokenExpirationTime();

            // Refresh Token을 Redis에 저장
            refreshTokenService.saveRefreshToken(restoredUser.getId().toString(), jwtRtToken);

            // 응답 생성
            UserRestoreResponseDto responseDto = UserRestoreResponseDto.fromEntity(
                    restoredUser, jwtAtToken, jwtRtToken, expiresIn
            );

            log.info("User restore successful: {}", restoredUser.getId());
            return ResponseDto.ok(responseDto, HttpStatus.OK);

        } catch (IllegalArgumentException e) {
            log.error("Invalid OAuth type: {}", requestDto.getOauthType(), e);
            return ResponseDto.fail(HttpStatus.BAD_REQUEST, "유효하지 않은 OAuth 타입입니다.");
        } catch (EntityNotFoundException e) {
            log.error("User not found for restore: {}", requestDto.getSocialId(), e);
            return ResponseDto.fail(HttpStatus.NOT_FOUND, "복구할 회원 정보를 찾을 수 없습니다.");
        } catch (Exception e) {
            log.error("User restore failed", e);
            return ResponseDto.fail(HttpStatus.INTERNAL_SERVER_ERROR, "회원 복구 처리 중 오류가 발생했습니다.");
        }
    }
}
