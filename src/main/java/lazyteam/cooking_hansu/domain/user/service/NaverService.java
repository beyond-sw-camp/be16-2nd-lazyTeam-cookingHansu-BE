package lazyteam.cooking_hansu.domain.user.service;

import lazyteam.cooking_hansu.domain.user.dto.NaverProfileDto;
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
public class NaverService implements OAuthService<NaverProfileDto> {

    @Value("${oauth.naver.client-id}")
    private String naverClientId;

    @Value("${oauth.naver.client-secret}")
    private String naverClientSecret;

    @Value("${oauth.naver.redirect-uri}")
    private String naverRedirectUri;

    @Override
    public CommonTokenDto getToken(String code){
//        인가코드, clientId, redirect_uri, grant_type

//        Spring6부터 RestTemplate 비추천상태이기에, 대신 RestClient 사용
        RestClient restClient = RestClient.create();
        String state = "";

//        MultiValueMap을 통해 자동으로 form-data형식으로 body 조립 가능
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", naverClientId);
        params.add("client_secret", naverClientSecret);
        params.add("state", state);
        params.add("grant_type", "authorization_code");

        ResponseEntity<CommonTokenDto> response = restClient.post()
                .uri("https://nid.naver.com/oauth2.0/token")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body(params)
                .retrieve()
                .toEntity(CommonTokenDto.class);

        log.info("Naver Token Response: {}", response.getBody());
        return response.getBody();
    }

    // Access Token을 통한 네이버 사용자 프로필 정보 가져오기
    @Override
    public NaverProfileDto getProfile(String token){
        RestClient restClient = RestClient.create();
        ResponseEntity<NaverProfileDto> response =  restClient.get()
                .uri("https://openapi.naver.com/v1/nid/me")
                .header("Authorization", "Bearer "+token)
                .retrieve()
                .toEntity(NaverProfileDto.class);

        log.info("Naver profile JSON: {}", response.getBody());
        return response.getBody();
    }

    @Override
    public String getProviderName() {
        return "naver";
    }
}