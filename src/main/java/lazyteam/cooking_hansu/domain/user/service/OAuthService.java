package lazyteam.cooking_hansu.domain.user.service;

import lazyteam.cooking_hansu.global.auth.dto.CommonTokenDto;

/**
 * OAuth 서비스 공통 인터페이스
 * 구글, 카카오, 네이버 => 다양한 OAuth 제공자를 위한 공통 메서드 정의
 */
public interface OAuthService<T> {

    /**
     * 인가 코드를 통한 액세스 토큰 발급
     */
    CommonTokenDto getToken(String code);

    /**
     * 액세스 토큰을 통한 사용자 프로필 정보 가져오는 메서드
     */
    T getProfile(String token);
}
