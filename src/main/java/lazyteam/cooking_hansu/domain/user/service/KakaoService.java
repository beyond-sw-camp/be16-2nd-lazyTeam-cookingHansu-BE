package lazyteam.cooking_hansu.domain.user.service;

import lazyteam.cooking_hansu.domain.user.dto.oauth.KakaoProfileDto;
import lazyteam.cooking_hansu.global.auth.dto.CommonTokenDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Service
@Slf4j
public class KakaoService implements OAuthService<KakaoProfileDto> {

    @Value("${oauth.kakao.client-id}")
    private String kakaoClientId;

    @Value("${oauth.kakao.redirect-uri}")
    private String kakaoRedirectUri;

    @Override
    public CommonTokenDto getToken(String code){
//        인가코드, clientId, redirect_uri, grant_type

//        Spring6부터 RestTemplate 비추천상태이기에, 대신 RestClient 사용
        RestClient restClient = RestClient.create();

//        MultiValueMap을 통해 자동으로 form-data형식으로 body 조립 가능
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", kakaoClientId);
        params.add("redirect_uri", kakaoRedirectUri);
        params.add("grant_type", "authorization_code");

        ResponseEntity<CommonTokenDto> response = restClient.post()
                .uri("https://kauth.kakao.com/oauth/token")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body(params)
                .retrieve()
                .toEntity(CommonTokenDto.class);

        log.info("Kakao Token Response: {}", response.getBody());
        return response.getBody();
    }

    // Access Token을 통한 카카오 사용자 프로필 정보 가져오기
    @Override
    public KakaoProfileDto getProfile(String token){
        RestClient restClient = RestClient.create();
        ResponseEntity<KakaoProfileDto> response =  restClient.get()
                .uri("https://kapi.kakao.com/v2/user/me")
                .header("Authorization", "Bearer "+token)
                .retrieve()
                .toEntity(KakaoProfileDto.class);

        log.info("Kakao profile JSON: {}", response.getBody());
        return response.getBody();
    }

    @Override
    public String getProviderName() {
        return "kakao";
    }
}