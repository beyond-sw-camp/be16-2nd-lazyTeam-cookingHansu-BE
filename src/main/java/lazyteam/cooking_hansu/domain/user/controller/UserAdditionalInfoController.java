package lazyteam.cooking_hansu.domain.user.controller;

import jakarta.validation.Valid;
import lazyteam.cooking_hansu.domain.user.dto.request.*;
import lazyteam.cooking_hansu.domain.user.dto.response.*;
import lazyteam.cooking_hansu.domain.user.service.UserAdditionalInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
     * 1단계 추가 정보 입력 (닉네임, 역할 선택)
     * POST /user/add-info/1
     */
    @PostMapping("/1")
    public ResponseEntity<UserAdditionalInfoStep1ResDto> updateStep1Info(
            @RequestParam UUID userId,
            @Valid @RequestBody UserAdditionalInfoStep1RequestDto requestDto) {

        UserAdditionalInfoStep1ResDto response = userAdditionalInfoService.updateStep1Info(userId, requestDto);
        return ResponseEntity.ok(response);
    }

    /**
     * 일반 회원 2단계 추가 정보 입력
     * POST /user/add-info/2/general
     */
    @PostMapping("/2/general")
    public ResponseEntity<UserAdditionalInfoStep2ResDto> updateGeneralUserStep2Info(
            @RequestParam UUID userId,
            @Valid @RequestBody GeneralUserStep2RequestDto requestDto) {

        UserAdditionalInfoStep2ResDto response = userAdditionalInfoService.updateGeneralUserStep2Info(userId, requestDto);
        return ResponseEntity.ok(response);
    }

    /**
     * 요식업 종사자 2단계 추가 정보 입력
     * POST /user/add-info/2/chef
     */
    @PostMapping("/2/chef")
    public ResponseEntity<UserAdditionalInfoStep2ResDto> updateChefUserStep2Info(
            @RequestParam UUID userId,
            @Valid @RequestBody ChefUserStep2RequestDto requestDto) {

        UserAdditionalInfoStep2ResDto response = userAdditionalInfoService.updateChefUserStep2Info(userId, requestDto);
        return ResponseEntity.ok(response);
    }

    /**
     * 요식업 자영업자 2단계 추가 정보 입력
     * POST /user/add-info/2/business
     */
    @PostMapping("/2/business")
    public ResponseEntity<UserAdditionalInfoStep2ResDto> updateBusinessUserStep2Info(
            @RequestParam UUID userId,
            @Valid @RequestBody BusinessUserStep2RequestDto requestDto) {

        UserAdditionalInfoStep2ResDto response = userAdditionalInfoService.updateBusinessUserStep2Info(userId, requestDto);
        return ResponseEntity.ok(response);
    }

    /**
     * 사용자 회원가입 상태 확인
     * GET /user/add-info/status
     */
    @GetMapping("/status")
    public ResponseEntity<UserRegistrationStatusResDto> getUserRegistrationStatus(
            @RequestParam UUID userId) {

        UserRegistrationStatusResDto response = userAdditionalInfoService.getUserRegistrationStatus(userId);
        return ResponseEntity.ok(response);
    }
}
