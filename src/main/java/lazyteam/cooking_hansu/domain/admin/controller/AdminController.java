package lazyteam.cooking_hansu.domain.admin.controller;

import lazyteam.cooking_hansu.domain.admin.dto.*;
import lazyteam.cooking_hansu.domain.admin.dto.request.AdminLoginReqDto;
import lazyteam.cooking_hansu.domain.admin.dto.request.AdminLogoutReqDto;
import lazyteam.cooking_hansu.domain.admin.dto.request.AdminRefreshTokenReqDto;
import lazyteam.cooking_hansu.domain.admin.dto.response.AdminRefreshTokenResDto;
import lazyteam.cooking_hansu.domain.admin.dto.response.DashBoardResDto;
import lazyteam.cooking_hansu.domain.admin.service.AdminService;
import lazyteam.cooking_hansu.global.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 관리자 컨트롤러
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    /**
     * 관리자 로그인
     */
    @PostMapping("/login")
    public ResponseEntity<?> loginAdmin(@RequestBody AdminLoginReqDto adminLoginReqDto) {
        try {
            AdminLoginDto loginResult = adminService.loginAdmin(adminLoginReqDto);
            return new ResponseEntity<>(ResponseDto.ok(loginResult, HttpStatus.OK), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(ResponseDto.fail(HttpStatus.UNAUTHORIZED, e.getMessage()), HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * 관리자 토큰 재발급
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody AdminRefreshTokenReqDto refreshTokenReqDto) {
        try {
            AdminRefreshTokenResDto refreshResult = adminService.refreshAdminToken(refreshTokenReqDto.getRefreshToken());
            return new ResponseEntity<>(ResponseDto.ok(refreshResult, HttpStatus.OK), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(ResponseDto.fail(HttpStatus.UNAUTHORIZED, e.getMessage()), HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * 관리자 로그아웃
     */
    @PostMapping("/logout")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> logoutAdmin(@RequestBody AdminLogoutReqDto logoutReqDto) {
        try {
            adminService.logoutAdmin(logoutReqDto.getRefreshToken());
            return new ResponseEntity<>(ResponseDto.ok("로그아웃이 성공적으로 처리되었습니다.", HttpStatus.OK), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(ResponseDto.fail(HttpStatus.BAD_REQUEST, e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAdminDashboard() {
        DashBoardResDto adminDashboard = adminService.getAdminDashboard();
        return new ResponseEntity<>(ResponseDto.ok(adminDashboard, HttpStatus.OK), HttpStatus.OK);
    }
}
