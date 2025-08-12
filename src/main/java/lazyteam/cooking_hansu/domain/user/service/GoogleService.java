package lazyteam.cooking_hansu.domain.user.service;

import lazyteam.cooking_hansu.domain.user.dto.GoogleProfileDto;
import lazyteam.cooking_hansu.global.auth.dto.CommonTokenDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

/**
 * 구글 OAuth 서비스 구현
 * 구글 OAuth로 액세스 토큰 및 사용자 프로필 정보 처리
 */
@Service
@Slf4j
public class GoogleService implements OAuthService<GoogleProfileDto> {

    @Value("${oauth.google.client-id}")
    private String googleClientId;

    @Value("${oauth.google.client-secret}")
    private String googleClientSecret;

    @Value("${oauth.google.redirect-uri}")
    private String googleRedirectUri;

    // 인가 코드를 통한 Access Token 발급
    @Override
    public CommonTokenDto getToken(String code) {
        RestClient restClient = RestClient.create();

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", googleClientId);
        params.add("client_secret", googleClientSecret);
        params.add("redirect_uri", googleRedirectUri);
        params.add("grant_type", "authorization_code");

        ResponseEntity<CommonTokenDto> response = restClient.post()
                .uri("https://oauth2.googleapis.com/token")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body(params)
                .retrieve()
                .toEntity(CommonTokenDto.class);

        log.info("Google Token Response: {}", response.getBody());
        return response.getBody();
    }

    @Override
    public GoogleProfileDto getProfile(String token) {
        RestClient restClient = RestClient.create();
        ResponseEntity<GoogleProfileDto> response = restClient.get()
                .uri("https://www.googleapis.com/oauth2/v2/userinfo")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .toEntity(GoogleProfileDto.class);

        log.info("Google profile JSON: {}", response.getBody());
        return response.getBody();
    }

    @Override
    public String getProviderName() {
        return "google";
    }
}
