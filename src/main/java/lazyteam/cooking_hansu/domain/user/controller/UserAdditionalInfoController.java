package lazyteam.cooking_hansu.domain.user.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lazyteam.cooking_hansu.domain.user.dto.request.UserAdditionalInfoRequestDto;
import lazyteam.cooking_hansu.domain.user.dto.response.UserAdditionalInfoResDto;
import lazyteam.cooking_hansu.domain.user.service.UserAdditionalInfoService;
import lazyteam.cooking_hansu.global.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@RestController
@RequestMapping("/user/add-info")
@RequiredArgsConstructor
public class UserAdditionalInfoController {

    private final UserAdditionalInfoService userAdditionalInfoService;

    /**
     * 회원 추가 정보 입력 (통합 엔드포인트)
     * POST /user/add-info
     */
    @PostMapping(consumes = "multipart/form-data")
    public ResponseDto<UserAdditionalInfoResDto> updateAdditionalInfo(
            @RequestParam @NotNull(message = "사용자 ID는 필수입니다") UUID userId,
            @RequestParam @NotBlank(message = "닉네임은 필수입니다")
            @Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하여야 합니다") String nickname,
            @Size(max = 200, message = "자기소개는 200자 이하여야 합니다.") String info,
            @RequestParam @NotNull(message = "역할 선택은 필수입니다") Role role,
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
            log.info("회원 추가 정보 입력 요청 - userId: {}, role: {}, nickname: {}, info: {}", userId, role, nickname, info);

            // RequestDto 생성
            UserAdditionalInfoRequestDto requestDto = UserAdditionalInfoRequestDto.builder()
                    .nickname(nickname)
                    .role(role)
                    .info(info)
                    .generalType(generalType)
                    .licenseNumber(licenseNumber)
                    .cuisineType(cuisineType)
                    .licenseFile(licenseFile)
                    .businessNumber(businessNumber)
                    .businessFile(businessFile)
                    .businessName(businessName)
                    .businessAddress(businessAddress)
                    .shopCategory(shopCategory)
                    .build();

            UserAdditionalInfoResDto response = userAdditionalInfoService.updateAdditionalInfo(userId, requestDto);

            log.info("회원 추가 정보 입력 성공 - userId: {}", userId);
            return ResponseDto.ok(response, HttpStatus.OK);

        } catch (IllegalArgumentException e) {
            log.error("회원 추가 정보 입력 실패 - 잘못된 요청: userId: {}, error: {}", userId, e.getMessage());
            return ResponseDto.fail(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (RuntimeException e) {
            log.error("회원 추가 정보 입력 실패 - 비즈니스 로직 오류: userId: {}, error: {}", userId, e.getMessage());
            return ResponseDto.fail(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            log.error("회원 추가 정보 입력 실패 - 시스템 오류: userId: {}, error: {}", userId, e.getMessage(), e);
            return ResponseDto.fail(HttpStatus.INTERNAL_SERVER_ERROR, "회원 정보 저장 중 오류가 발생했습니다.");
        }
    }
}
