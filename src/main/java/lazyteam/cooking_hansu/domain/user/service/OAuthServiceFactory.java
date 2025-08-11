package lazyteam.cooking_hansu.domain.user.service;

import lazyteam.cooking_hansu.domain.user.entity.common.OauthType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * OAuth 서비스 팩토리
 * 다양한 OAuth 제공자 서비스 관리 & 제공
 */
@Component
@RequiredArgsConstructor
public class OAuthServiceFactory {

    private final GoogleService googleService;
    private final KakaoService kakaoService;

    /**
     * OAuth 타입에 따른 적절한 서비스 반환
     * @param oauthType OAuth 제공자 타입
     * @return 해당 OAuth 서비스
     * @throws IllegalArgumentException 지원하지 않는 OAuth 타입인 경우
     */
    public OAuthService getOAuthService(OauthType oauthType) {
        return switch (oauthType) {
            case GOOGLE -> googleService;
            case KAKAO -> kakaoService;
//            case NAVER -> naverService;
            default -> throw new IllegalArgumentException("지원하지 않는 OAuth 타입입니다: " + oauthType);
        };
    }

    /**
     * 제공자 이름으로 OAuth 서비스 반환
     * @param providerName 제공자 이름 (예: "google", "kakao", "naver")
     * @return 해당 OAuth 서비스
     * @throws IllegalArgumentException 지원하지 않는 제공자인 경우
     */
    public OAuthService getOAuthService(String providerName) {
        return switch (providerName.toLowerCase()) {
            case "google" -> googleService;
            case "kakao" -> kakaoService;
//            case "naver" -> naverService;
            default -> throw new IllegalArgumentException("지원하지 않는 OAuth 제공자입니다: " + providerName);
        };
    }

    /**
     * 사용 가능한 모든 OAuth 서비스 반환
     * @return OAuth 서비스 맵 (제공자 이름 -> 서비스)
     */
    public Map<String, OAuthService> getAllOAuthServices() {
        return Map.of(
            "google", googleService,
            "kakao", kakaoService
//            "naver", naverService
        );
    }
}
