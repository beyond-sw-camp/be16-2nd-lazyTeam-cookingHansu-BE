package lazyteam.cooking_hansu.domain.user.service;

import lazyteam.cooking_hansu.global.auth.dto.GoogleTokenDto;
import lazyteam.cooking_hansu.domain.user.dto.GoogleProfileDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Service
@Slf4j
public class GoogleService {

    @Value("${oauth.google.client-id}")
    private String googleClientId;

    @Value("${oauth.google.client-secret}")
    private String googleClientSecret;

    @Value("${oauth.google.redirect-uri}")
    private String googleRedirectUri;


    public GoogleTokenDto getToken(String code){
//        인가코드, clientId, client_secret, redirect_uri, grant_type

//        Spring6부터 RestTemplate 비추천상태이기에, 대신 RestClient 사용
        RestClient restClient = RestClient.create();

//        MultiValueMap을 통해 자동으로 form-data형식으로 body 조립 가능
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", googleClientId);
        params.add("client_secret", googleClientSecret);
        params.add("redirect_uri", googleRedirectUri);
        params.add("grant_type", "authorization_code");

        ResponseEntity<GoogleTokenDto> response = restClient.post()
                .uri("https://oauth2.googleapis.com/token")
                .header("Content-Type", "application/x-www-form-urlencoded")
//                ?code=xxxx&client_id=yyyy&
                .body(params)
//                retrieve:응답 body값만을 추출
                .retrieve()
                .toEntity(GoogleTokenDto.class);

        log.info("Google Token Response: {}", response.getBody());
        return response.getBody();
    }

    public GoogleProfileDto getGoogleProfile(String token){
        RestClient restClient = RestClient.create();
        ResponseEntity<GoogleProfileDto> response =  restClient.get()
                .uri("https://openidconnect.googleapis.com/v1/userinfo")
                .header("Authorization", "Bearer "+token)
                .retrieve()
                .toEntity(GoogleProfileDto.class);

        log.info("profile JSON: {}", response.getBody());
        return response.getBody();
    }
}