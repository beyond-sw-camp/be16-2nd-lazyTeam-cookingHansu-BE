package lazyteam.cooking_hansu.domain.user.controller;

import lazyteam.cooking_hansu.domain.user.dto.RedirectDto;
import lazyteam.cooking_hansu.domain.user.dto.UserLoginDto;
import lazyteam.cooking_hansu.domain.user.dto.oauth.GoogleProfileDto;
import lazyteam.cooking_hansu.domain.user.dto.oauth.KakaoProfileDto;
import lazyteam.cooking_hansu.domain.user.dto.oauth.NaverProfileDto;
import lazyteam.cooking_hansu.domain.user.entity.common.OauthType;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lazyteam.cooking_hansu.domain.user.service.GoogleService;
import lazyteam.cooking_hansu.domain.user.service.KakaoService;
import lazyteam.cooking_hansu.domain.user.service.NaverService;
import lazyteam.cooking_hansu.domain.user.service.UserService;
import lazyteam.cooking_hansu.global.auth.JwtTokenProvider;
import lazyteam.cooking_hansu.global.auth.dto.CommonTokenDto;
import lazyteam.cooking_hansu.global.dto.ResponseDto;
import lazyteam.cooking_hansu.global.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user/login")
@Slf4j
public class LoginController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final GoogleService googleService;
    private final KakaoService kakaoService;
    private final NaverService naverService;

    // 구글 로그인 요청 처리
    @PostMapping("/google")
    public ResponseDto<?> googleLogin(@RequestBody RedirectDto redirectDto) {
        try {
            // access token & refresh token 발급
            CommonTokenDto commonTokenDto = googleService.getToken(redirectDto.getCode());

            // 사용자 정보 얻기
            GoogleProfileDto googleProfileDto = googleService.getProfile(commonTokenDto.getAccess_token());

            // 기존 사용자 조회
            User originalUser = userService.getUserBySocialId(googleProfileDto.getSub());

            // 회원 가입이 되어 있지 않다면 회원가입
            if (originalUser == null) {
                originalUser = userService.createGoogleOauth(
                        googleProfileDto.getSub(),
                        googleProfileDto.getName(),
                        googleProfileDto.getEmail(),
                        googleProfileDto.getPicture(),
                        OauthType.GOOGLE
                );
            }

            return createLoginResponse(originalUser);
        } catch (Exception e) {
            log.error("Google login failed", e);
            return ResponseDto.fail(HttpStatus.INTERNAL_SERVER_ERROR, "구글 로그인 처리 중 오류가 발생했습니다.");
        }
    }

    // 카카오 로그인 요청 처리
    @PostMapping("/kakao")
    public ResponseDto<?> kakaoLogin(@RequestBody RedirectDto redirectDto) {
        try {
            // access token & refresh token 발급
            CommonTokenDto commonTokenDto = kakaoService.getToken(redirectDto.getCode());

            // 사용자 정보 얻기
            KakaoProfileDto kakaoProfileDto = kakaoService.getProfile(commonTokenDto.getAccess_token());

            // 기존 사용자 조회
            User originalUser = userService.getUserBySocialId(kakaoProfileDto.getId());

            // 회원 가입이 되어 있지 않다면 회원가입
            if (originalUser == null) {
                originalUser = userService.createKakaoOauth(
                        kakaoProfileDto.getId(),
                        kakaoProfileDto.getKakao_account().getName(),
                        kakaoProfileDto.getKakao_account().getEmail(),
                        kakaoProfileDto.getKakao_account().getProfile().getProfile_image_url(),
                        OauthType.KAKAO
                );
            }

            return createLoginResponse(originalUser);
        } catch (Exception e) {
            log.error("Kakao login failed", e);
            return ResponseDto.fail(HttpStatus.INTERNAL_SERVER_ERROR, "카카오 로그인 처리 중 오류가 발생했습니다.");
        }
    }

    // 네이버 로그인 요청 처리
    @PostMapping("/naver")
    public ResponseDto<?> naverLogin(@RequestBody RedirectDto redirectDto) {
        try {
            // access token & refresh token 발급
            CommonTokenDto commonTokenDto = naverService.getToken(redirectDto.getCode());

            // 사용자 정보 얻기
            NaverProfileDto naverProfileDto = naverService.getProfile(commonTokenDto.getAccess_token());

            // 기존 사용자 조회
            User originalUser = userService.getUserBySocialId(naverProfileDto.getResponse().getId());

            // 회원 가입이 되어 있지 않다면 회원가입
            if (originalUser == null) {
                originalUser = userService.createNaverOauth(
                        naverProfileDto.getResponse().getId(),
                        naverProfileDto.getResponse().getName(),
                        naverProfileDto.getResponse().getEmail(),
                        naverProfileDto.getResponse().getProfile_image(),
                        OauthType.NAVER
                );
            }

            return createLoginResponse(originalUser);
        } catch (Exception e) {
            log.error("Naver login failed", e);
            return ResponseDto.fail(HttpStatus.INTERNAL_SERVER_ERROR, "네이버 로그인 처리 중 오류가 발생했습니다.");
        }
    }

    /**
     * 로그인 응답 생성 공통 메서드
     * @param user 사용자
     * @return 로그인 응답
     */
    private ResponseDto<?> createLoginResponse(User user) {
        // JWT 토큰 발급
        String jwtAtToken = jwtTokenProvider.createAtToken(user);
        String jwtRtToken = jwtTokenProvider.createRtToken(user);
        Long expiresIn = jwtTokenProvider.getRefreshTokenExpirationTime();

        // Refresh Token을 Redis에 저장
        refreshTokenService.saveRefreshToken(user.getId().toString(), jwtRtToken);

        // 프론트엔드 요구사항에 맞는 응답 데이터 생성 (isNewUser는 동적으로 계산됨)
        UserLoginDto userLoginDto = UserLoginDto.fromEntity(user, jwtAtToken, jwtRtToken, expiresIn);

        return ResponseDto.ok(userLoginDto, HttpStatus.OK);
    }
}
