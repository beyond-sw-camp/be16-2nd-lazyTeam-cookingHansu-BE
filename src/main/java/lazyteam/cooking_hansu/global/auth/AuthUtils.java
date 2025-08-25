package lazyteam.cooking_hansu.global.auth;

import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lazyteam.cooking_hansu.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AuthUtils {
    
    private final UserRepository userRepository;

     // 현재 인증된 사용자의 이메일 반환
    public String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new IllegalStateException("인증된 사용자 정보가 없습니다.");
        }
        return authentication.getName();
    }
    

     // 현재 인증된 사용자의 ID 반환
    public UUID getCurrentUserId() {
        String email = getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("사용자를 찾을 수 없습니다: " + email));
        return user.getId();
    }

     // 현재 인증된 사용자 엔티티 반환
    public User getCurrentUser() {
        String email = getCurrentUserEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("사용자를 찾을 수 없습니다: " + email));
    }
}

