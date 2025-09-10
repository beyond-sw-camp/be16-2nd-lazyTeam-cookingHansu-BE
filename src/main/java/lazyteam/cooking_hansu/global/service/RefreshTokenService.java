package lazyteam.cooking_hansu.global.service;

import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lazyteam.cooking_hansu.domain.user.repository.UserRepository;
import lazyteam.cooking_hansu.global.auth.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class RefreshTokenService {

    private final RedisTemplate<String, String> redisTemplate;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    public RefreshTokenService(@Qualifier("rtTemplate") RedisTemplate<String, String> redisTemplate,
                              JwtTokenProvider jwtTokenProvider,
                              UserRepository userRepository) {
        this.redisTemplate = redisTemplate;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
    }

    // Refresh Token 저장 (TTL 설정)
    public void saveRefreshToken(String userId, String refreshToken) {
        try {
            long expirationInMillis = jwtTokenProvider.getRefreshTokenExpirationTime();
            long expirationInSeconds = expirationInMillis / 1000L; // 밀리초를 초로 변환
            redisTemplate.opsForValue().set(
                "refresh_token:" + userId,
                refreshToken,
                expirationInSeconds,
                TimeUnit.SECONDS
            );
            log.info("Refresh token saved for user: {} with expiration: {} seconds", userId, expirationInSeconds);
        } catch (Exception e) {
            log.error("Failed to save refresh token for user: {}", userId, e);
            throw new RuntimeException("Refresh token 저장에 실패했습니다.");
        }
    }

    // Refresh Token으로 사용자 조회
    public User getRefreshToken(String refreshToken) {
        try {
            // 토큰 유효성 검증
            if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
                log.warn("Invalid refresh token provided");
                return null;
            }

            // 토큰에서 아이디 추출
            String id = jwtTokenProvider.getIdFromRefreshToken(refreshToken);
            if (id == null) {
                log.warn("Failed to extract email from refresh token");
                return null;
            }

            // 사용자 조회
            User user = userRepository.findById(UUID.fromString(id)).orElse(null);
            if (user == null) {
                log.warn("User not found for email: {}", id);
                return null;
            }

            // Redis에서 저장된 토큰과 비교
            String storedToken = redisTemplate.opsForValue().get("refresh_token:" + user.getId().toString());
            if (storedToken == null || !storedToken.equals(refreshToken)) {
                log.warn("Refresh token mismatch for user: {}", user.getId());
                return null;
            }

            return user;
        } catch (Exception e) {
            log.error("Failed to get user from refresh token", e);
            return null;
        }
    }

    // Refresh Token 삭제
    public void deleteRefreshToken(String userId) {
        try {
            redisTemplate.delete("refresh_token:" + userId);
            log.info("Refresh token deleted for user: {}", userId);
        } catch (Exception e) {
            log.error("Failed to delete refresh token for user: {}", userId, e);
        }
    }

    // 관리자용: 저장된 Refresh Token 문자열 직접 조회
    public String getStoredRefreshToken(String userId) {
        try {
            String storedToken = redisTemplate.opsForValue().get("refresh_token:" + userId);
            log.info("Retrieved refresh token for user: {}", userId);
            return storedToken;
        } catch (Exception e) {
            log.error("Failed to get stored refresh token for user: {}", userId, e);
            return null;
        }
    }

    // 새로운 Refresh Token으로 갱신
    public void updateRefreshToken(String userId, String newRefreshToken) {
        try {
            // 기존 토큰 삭제
            deleteRefreshToken(userId);
            // 새 토큰 저장
            saveRefreshToken(userId, newRefreshToken);
            log.info("Refresh token updated for user: {}", userId);
        } catch (Exception e) {
            log.error("Failed to update refresh token for user: {}", userId, e);
            throw new RuntimeException("Refresh token 갱신에 실패했습니다.");
        }
    }

    // 사용자의 모든 Refresh Token 확인
    public boolean hasRefreshToken(String userId) {
        try {
            return redisTemplate.hasKey("refresh_token:" + userId);
        } catch (Exception e) {
            log.error("Failed to check refresh token existence for user: {}", userId, e);
            return false;
        }
    }
}
