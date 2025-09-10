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

    // 새 메서드 (비회원 허용)
    public static UUID getCurrentUserIdOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null) {
            return null;
        }

        // 관리자인 경우 null 반환 (User 테이블과 분리된 Admin 테이블 사용)
        if (isAdmin()) {
            return null;
        }

        try {
            return UUID.fromString(authentication.getName());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * 현재 사용자가 관리자인지 확인
     */
    public static boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || authentication.getAuthorities() == null) {
            return false;
        }
        
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }
}