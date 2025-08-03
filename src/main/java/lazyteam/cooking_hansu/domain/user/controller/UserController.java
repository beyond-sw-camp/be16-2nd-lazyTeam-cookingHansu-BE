package lazyteam.cooking_hansu.domain.user.controller;

import lazyteam.cooking_hansu.domain.user.dto.AccessTokenDto;
import lazyteam.cooking_hansu.domain.user.dto.GoogleProfileDto;
import lazyteam.cooking_hansu.domain.user.dto.RedirectDto;
import lazyteam.cooking_hansu.domain.user.entity.common.OauthType;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lazyteam.cooking_hansu.domain.user.repository.UserRepository;
import lazyteam.cooking_hansu.domain.user.service.GoogleService;
import lazyteam.cooking_hansu.domain.user.service.UserService;
import lazyteam.cooking_hansu.global.auth.JwtTokenProvider;
import lazyteam.cooking_hansu.global.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final GoogleService googleService;

    // TODO: 회원 관련 API 메서드 구현 예정

    // 구글 로그인 요청 처리
    @PostMapping("/google/login")
    public ResponseEntity<?> googleLogin(@RequestBody RedirectDto redirectDto) {
        // access token 발급
        AccessTokenDto accessTokenDto = googleService.getAccessToken(redirectDto.getCode());

        // 사용자 정보 얻기
        GoogleProfileDto googleProfileDto = googleService.getGoogleProfile(accessTokenDto.getAccess_token());

        // 회원가입이 되어 있지 않다면 회원가입
        User originalUser = userService.getUserBySocialId(googleProfileDto.getSub());
        if (originalUser == null) {
            originalUser = userService.createOauth(googleProfileDto.getSub(), googleProfileDto.getEmail(), OauthType.GOOGLE);
        }

        // 회원 가입 되어있다면 토큰 발급
        String jwtToken = jwtTokenProvider.createAtToken(originalUser);

        return new ResponseEntity(ResponseDto.ok(jwtToken, HttpStatus.OK), HttpStatus.OK);
    }
}
