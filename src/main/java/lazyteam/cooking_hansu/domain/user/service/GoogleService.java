package lazyteam.cooking_hansu.domain.user.service;

import lazyteam.cooking_hansu.domain.user.dto.AccessTokenDto;
import lazyteam.cooking_hansu.domain.user.dto.GoogleProfileDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class GoogleService {

    @Value("${oauth.google.client-id}")
    private String googleClientId;

    @Value("${oauth.google.client-secret}")
    private String googleClientSecret;

    @Value("${oauth.google.redirect-uri}")
    private String googleRedirectUri;

    public AccessTokenDto getAccessToken(String code) {
        // 인가 코드, cliendId, client_secret, redirect_uri, grant_type

        RestClient restClient = RestClient.create();

        // MultiValueMap을 통해 자동으로 form-data 형식으로 body 조립 가능
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", googleClientId);
        params.add("client_secret", googleClientSecret);
        params.add("redirect_uri", googleRedirectUri);
        params.add("grant_type", "authorization_code");

        ResponseEntity<AccessTokenDto> response = restClient.post()
                .uri("https://oauth2.googleapis.com/token")
                .header("Content-Type", "application/x-www-form-urlencoded")
                // ?code=xxxx&client_id=yyyy&...
                .body(params)
                // 응답 body 값만을 추출
                .retrieve()
                .toEntity(AccessTokenDto.class);

        return response.getBody();
    }

    public GoogleProfileDto getGoogleProfile(String token) {
        RestClient restClient = RestClient.create();

        ResponseEntity<GoogleProfileDto> response = restClient.get()
                .uri("https://openidconnect.googleapis.com/v1/userinfo")
                .header("Authorization", token)
                // 응답 body 값만을 추출
                .retrieve()
                .toEntity(GoogleProfileDto.class);

        return response.getBody();
    }
}
