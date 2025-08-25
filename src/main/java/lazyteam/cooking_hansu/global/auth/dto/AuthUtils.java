package lazyteam.cooking_hansu.global.auth.dto;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;


public class AuthUtils {

    /**
     * SecurityContext에서 현재 로그인한 유저의 UUID 추출
     */
    public static UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null) {
            throw new IllegalStateException("인증 정보가 존재하지 않습니다.");
        }

        try {
            return UUID.fromString(authentication.getName());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Authentication name이 UUID 형식이 아닙니다: " + authentication.getName());
        }
    }
}