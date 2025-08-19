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
import org.springframework.web.multipart.MultipartFile;
import lazyteam.cooking_hansu.domain.user.entity.common.Role;
import lazyteam.cooking_hansu.domain.user.entity.common.GeneralType;
import lazyteam.cooking_hansu.domain.user.entity.chef.CuisineType;

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
     * 회원 추가 정보 입력 (JSON 방식) - 파일 없이 기본 정보만
     * POST /user/add-info/json
     */
    @PostMapping(value = "/json", consumes = "application/json")
    public ResponseDto<UserAdditionalInfoResponseDto> updateAdditionalInfoJson(
            @Valid @RequestBody UserAdditionalInfoRequestDto requestDto,
            @RequestParam UUID userId) {

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
     * 회원 추가 정보 입력 (Multipart 방식) - 파일 업로드 지원
     * POST /user/add-info/multipart
     */
    @PostMapping(value = "/multipart", consumes = "multipart/form-data")
    public ResponseDto<UserAdditionalInfoResponseDto> updateAdditionalInfoMultipart(
            @RequestParam UUID userId,
            @RequestParam(required = false) String nickname,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) GeneralType generalType,
            @RequestParam(required = false) String licenseNumber,
            @RequestParam(required = false) CuisineType cuisineType,
            @RequestPart(required = false) MultipartFile licenseFile,
            @RequestParam(required = false) String businessNumber,
            @RequestPart(required = false) MultipartFile businessFile,
            @RequestParam(required = false) String businessName,
            @RequestParam(required = false) String businessAddress,
            @RequestParam(required = false) String shopCategory) {

        try {
            // RequestDto 생성
            UserAdditionalInfoRequestDto requestDto = new UserAdditionalInfoRequestDto(
                    nickname, role, generalType, licenseNumber, cuisineType, licenseFile,
                    businessNumber, businessFile, businessName, businessAddress, shopCategory
            );

            UserAdditionalInfoResponseDto response = userAdditionalInfoService.updateAdditionalInfo(userId, requestDto);
            return ResponseDto.ok(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            return ResponseDto.fail(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            return ResponseDto.fail(HttpStatus.INTERNAL_SERVER_ERROR, "회원 정보 저장 중 오류가 발생했습니다.");
        }
    }

    /**
     * 기존 엔드포인트 (하위 호환성) - 자동으로 적절한 방식 선택
     * POST /user/add-info
     */
    @PostMapping
    public ResponseDto<UserAdditionalInfoResponseDto> updateAdditionalInfo(
            @RequestParam UUID userId,
            @RequestParam(required = false) String nickname,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) GeneralType generalType,
            @RequestParam(required = false) String licenseNumber,
            @RequestParam(required = false) CuisineType cuisineType,
            @RequestPart(required = false) MultipartFile licenseFile,
            @RequestParam(required = false) String businessNumber,
            @RequestPart(required = false) MultipartFile businessFile,
            @RequestParam(required = false) String businessName,
            @RequestParam(required = false) String businessAddress,
            @RequestParam(required = false) String shopCategory) {

        try {
            // RequestDto 생성
            UserAdditionalInfoRequestDto requestDto = new UserAdditionalInfoRequestDto(
                    nickname, role, generalType, licenseNumber, cuisineType, licenseFile,
                    businessNumber, businessFile, businessName, businessAddress, shopCategory
            );

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
