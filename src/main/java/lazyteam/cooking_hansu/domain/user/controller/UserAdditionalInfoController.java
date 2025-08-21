package lazyteam.cooking_hansu.domain.user.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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
     * 회원 추가 정보 입력 (JSON 방식) - 파일 없이 기본 정보만
     * POST /user/add-info/json
     */
    @PostMapping(value = "/json", consumes = "application/json")
    public ResponseDto<UserAdditionalInfoResDto> updateAdditionalInfoJson(
            @Valid @RequestBody UserAdditionalInfoRequestDto requestDto,
            @RequestParam UUID userId) {

        try {
            log.info("JSON 방식 회원 추가 정보 입력 요청 - userId: {}, role: {}, nickname: {}",
                    userId, requestDto.getRole(), requestDto.getNickname());

            UserAdditionalInfoResDto response = userAdditionalInfoService.updateAdditionalInfo(userId, requestDto);

            log.info("JSON 방식 회원 추가 정보 입력 성공 - userId: {}", userId);
            return ResponseDto.ok(response, HttpStatus.OK);

        } catch (IllegalArgumentException e) {
            log.error("JSON 방식 회원 추가 정보 입력 실패 - 잘못된 요청: userId: {}, error: {}", userId, e.getMessage());
            return ResponseDto.fail(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (RuntimeException e) {
            log.error("JSON 방식 회원 추가 정보 입력 실패 - 비즈니스 로직 오류: userId: {}, error: {}", userId, e.getMessage());
            return ResponseDto.fail(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            log.error("JSON 방식 회원 추가 정보 입력 실패 - 시스템 오류: userId: {}, error: {}", userId, e.getMessage(), e);
            return ResponseDto.fail(HttpStatus.INTERNAL_SERVER_ERROR, "회원 정보 저장 중 오류가 발생했습니다.");
        }
    }

    /**
     * 회원 추가 정보 입력 (Multipart 방식) - 파일 업로드 지원
     * POST /user/add-info/multipart
     */
    @PostMapping(value = "/multipart", consumes = "multipart/form-data")
    public ResponseDto<UserAdditionalInfoResDto> updateAdditionalInfoMultipart(
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
            log.info("Multipart 방식 회원 추가 정보 입력 요청 - userId: {}, role: {}, nickname: {}",
                    userId, role, nickname);

            // RequestDto 생성
            UserAdditionalInfoRequestDto requestDto = new UserAdditionalInfoRequestDto(
                    nickname, role, generalType, licenseNumber, cuisineType, licenseFile,
                    businessNumber, businessFile, businessName, businessAddress, shopCategory
            );

            UserAdditionalInfoResDto response = userAdditionalInfoService.updateAdditionalInfo(userId, requestDto);

            log.info("Multipart 방식 회원 추가 정보 입력 성공 - userId: {}", userId);
            return ResponseDto.ok(response, HttpStatus.OK);

        } catch (IllegalArgumentException e) {
            log.error("Multipart 방식 회원 추가 정보 입력 실패 - 잘못된 요청: userId: {}, error: {}", userId, e.getMessage());
            return ResponseDto.fail(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (RuntimeException e) {
            log.error("Multipart 방식 회원 추가 정보 입력 실패 - 비즈니스 로직 오류: userId: {}, error: {}", userId, e.getMessage());
            return ResponseDto.fail(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            log.error("Multipart 방식 회원 추가 정보 입력 실패 - 시스템 오류: userId: {}, error: {}", userId, e.getMessage(), e);
            return ResponseDto.fail(HttpStatus.INTERNAL_SERVER_ERROR, "회원 정보 저장 중 오류가 발생했습니다.");
        }
    }

    /**
     * 기존 엔드포인트 (하위 호환성) - JSON과 multipart 모두 지원
     * POST /user/add-info
     */
    @PostMapping
    public ResponseDto<UserAdditionalInfoResDto> updateAdditionalInfo(
            HttpServletRequest request,
            @RequestParam UUID userId,
            @RequestBody(required = false) UserAdditionalInfoRequestDto jsonRequestDto,
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
            String contentType = request.getContentType();
            log.info("기존 엔드포인트 회원 추가 정보 입력 요청 - userId: {}, Content-Type: {}", userId, contentType);

            UserAdditionalInfoRequestDto requestDto;

            // Content-Type에 따라 처리 방식 결정
            if (contentType != null && contentType.contains("application/json")) {
                // JSON 방식
                if (jsonRequestDto == null) {
                    log.error("JSON 요청이지만 요청 데이터가 null입니다 - userId: {}", userId);
                    return ResponseDto.fail(HttpStatus.BAD_REQUEST, "요청 데이터가 없습니다.");
                }
                log.info("JSON 형태 요청 데이터 처리 - userId: {}, role: {}, nickname: {}",
                        userId, jsonRequestDto.getRole(), jsonRequestDto.getNickname());
                requestDto = jsonRequestDto;
            } else {
                // Multipart 방식
                log.info("Multipart 형태 요청 데이터 처리 - userId: {}, role: {}, nickname: {}",
                        userId, role, nickname);
                requestDto = new UserAdditionalInfoRequestDto(
                        nickname, role, generalType, licenseNumber, cuisineType, licenseFile,
                        businessNumber, businessFile, businessName, businessAddress, shopCategory
                );
            }

            UserAdditionalInfoResDto response = userAdditionalInfoService.updateAdditionalInfo(userId, requestDto);

            log.info("기존 엔드포인트 회원 추가 정보 입력 성공 - userId: {}", userId);
            return ResponseDto.ok(response, HttpStatus.OK);

        } catch (IllegalArgumentException e) {
            log.error("기존 엔드포인트 회원 추가 정보 입력 실패 - 잘못된 요청: userId: {}, error: {}", userId, e.getMessage());
            return ResponseDto.fail(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (RuntimeException e) {
            log.error("기존 엔드포인트 회원 추가 정보 입력 실패 - 비즈니스 로직 오류: userId: {}, error: {}", userId, e.getMessage());
            return ResponseDto.fail(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            log.error("기존 엔드포인트 회원 추가 정보 입력 실패 - 시스템 오류: userId: {}, error: {}", userId, e.getMessage(), e);
            return ResponseDto.fail(HttpStatus.INTERNAL_SERVER_ERROR, "회원 정보 저장 중 오류가 발생했습니다.");
        }
    }
}
