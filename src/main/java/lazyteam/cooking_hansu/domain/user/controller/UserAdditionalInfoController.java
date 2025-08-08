package lazyteam.cooking_hansu.domain.user.controller;

import jakarta.validation.Valid;
import lazyteam.cooking_hansu.domain.user.dto.request.UserAdditionalInfoRequestDto;
import lazyteam.cooking_hansu.domain.user.dto.response.UserAdditionalInfoResponseDto;
import lazyteam.cooking_hansu.domain.user.dto.response.UserRegistrationStatusResDto;
import lazyteam.cooking_hansu.domain.user.service.UserAdditionalInfoService;
import lazyteam.cooking_hansu.global.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * 회원 추가 정보 입력 컨트롤러
 */
@RestController
@RequestMapping("/user/add-info")
@RequiredArgsConstructor
public class UserAdditionalInfoController {

    private final UserAdditionalInfoService userAdditionalInfoService;

    /**
     * 회원 추가 정보 입력 (통합)
     * POST /user/add-info
     */
    @PostMapping
    public ResponseDto<UserAdditionalInfoResponseDto> updateAdditionalInfo(
            @RequestParam UUID userId,
            @Valid @RequestBody UserAdditionalInfoRequestDto requestDto) {

        try {
            UserAdditionalInfoResponseDto response = userAdditionalInfoService.updateAdditionalInfo(userId, requestDto);
            return ResponseDto.ok(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            return ResponseDto.fail(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            return ResponseDto.fail(HttpStatus.INTERNAL_SERVER_ERROR, "회원 정보 저장 중 오류가 발생했습니다.");
        }
    }

    /**
     * 사용자 회원가입 상태 확인
     * GET /user/add-info/status
     */
    @GetMapping("/status")
    public ResponseDto<UserRegistrationStatusResDto> getUserRegistrationStatus(
            @RequestParam UUID userId) {

        try {
            UserRegistrationStatusResDto response = userAdditionalInfoService.getUserRegistrationStatus(userId);
            return ResponseDto.ok(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            return ResponseDto.fail(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            return ResponseDto.fail(HttpStatus.INTERNAL_SERVER_ERROR, "회원가입 상태 조회 중 오류가 발생했습니다.");
        }
    }
}
