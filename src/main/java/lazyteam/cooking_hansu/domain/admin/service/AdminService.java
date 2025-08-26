package lazyteam.cooking_hansu.domain.admin.service;

import lazyteam.cooking_hansu.domain.admin.dto.AdminLoginDto;
import lazyteam.cooking_hansu.domain.admin.dto.request.AdminLoginReqDto;
import lazyteam.cooking_hansu.domain.admin.dto.response.AdminRefreshTokenResDto;
import lazyteam.cooking_hansu.domain.admin.dto.response.DashBoardResDto;
import lazyteam.cooking_hansu.domain.admin.entity.Admin;
import lazyteam.cooking_hansu.domain.admin.repository.AdminRepository;
import lazyteam.cooking_hansu.domain.common.enums.ApprovalStatus;
import lazyteam.cooking_hansu.domain.lecture.repository.LectureRepository;
import lazyteam.cooking_hansu.domain.user.repository.OwnerRepository;
import lazyteam.cooking_hansu.domain.user.repository.ChefRepository;
import lazyteam.cooking_hansu.domain.user.repository.UserRepository;
import lazyteam.cooking_hansu.global.auth.JwtTokenProvider;
import lazyteam.cooking_hansu.global.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 관리자 서비스 클래스
 * 관리자 로그인, 토큰 재발급, 로그아웃, 대시보드 정보 조회 등 로직 처리
 */
@Service
@Transactional
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final ChefRepository chefRepository;
    private final OwnerRepository ownerRepository;
    private final LectureRepository lectureRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    /**
     * 관리자 로그인
     * email & password + JWT 토큰 방식
     * @param adminLoginReqDto
     * @return
     */
    public AdminLoginDto loginAdmin(AdminLoginReqDto adminLoginReqDto) {
        Optional<Admin> optAdmin = adminRepository.findByEmail(adminLoginReqDto.getEmail());

        if (!optAdmin.isPresent()) {
            throw new IllegalArgumentException("관리자 email이 존재하지 않습니다.");
        }

        Admin admin = optAdmin.get();
        if (!passwordEncoder.matches(adminLoginReqDto.getPassword(), admin.getPassword())) {
            throw new IllegalArgumentException("관리자 password가 일치하지 않습니다.");
        }

        // JWT 토큰 생성
        String adminAtToken = jwtTokenProvider.createAdminAtToken(admin);
        String adminRtToken = jwtTokenProvider.createAdminRtToken(admin);
        Long expiresIn = jwtTokenProvider.getRefreshTokenExpirationTime();

        // Refresh Token을 Redis에 저장
        refreshTokenService.saveRefreshToken(admin.getId().toString(), adminRtToken);

        return AdminLoginDto.fromEntity(admin, adminAtToken, adminRtToken, expiresIn);
    }

    /**
     * 관리자 토큰 재발급
     * @param refreshToken
     * @return
     */
    public AdminRefreshTokenResDto refreshAdminToken(String refreshToken) {
        // Refresh Token 유효성 검증
        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 refresh token입니다.");
        }

        // Refresh Token에서 관리자 ID 추출
        String adminId = jwtTokenProvider.getIdFromRefreshToken(refreshToken);
        if (adminId == null) {
            throw new IllegalArgumentException("refresh token에서 관리자 ID를 추출할 수 없습니다.");
        }

        // Redis에서 저장된 Refresh Token과 비교
        String storedRefreshToken = refreshTokenService.getStoredRefreshToken(adminId);
        if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
            throw new IllegalArgumentException("저장된 refresh token과 일치하지 않습니다.");
        }

        // 관리자 정보 조회
        Optional<Admin> optAdmin = adminRepository.findById(java.util.UUID.fromString(adminId));
        if (!optAdmin.isPresent()) {
            throw new IllegalArgumentException("관리자 정보를 찾을 수 없습니다.");
        }

        Admin admin = optAdmin.get();

        // 새로운 토큰 생성
        String newAccessToken = jwtTokenProvider.createAdminAtToken(admin);
        String newRefreshToken = jwtTokenProvider.createAdminRtToken(admin);
        Long expiresIn = jwtTokenProvider.getRefreshTokenExpirationTime();

        // 새로운 Refresh Token을 Redis에 저장
        refreshTokenService.saveRefreshToken(adminId, newRefreshToken);

        return AdminRefreshTokenResDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .expiresIn(expiresIn)
                .build();
    }

    /**
     * 관리자 로그아웃
     * @param refreshToken
     */
    public void logoutAdmin(String refreshToken) {
        // Refresh Token에서 관리자 ID 추출
        String adminId = jwtTokenProvider.getIdFromRefreshToken(refreshToken);
        if (adminId == null) {
            throw new IllegalArgumentException("refresh token에서 관리자 ID를 추출할 수 없습니다.");
        }

        // Redis에서 Refresh Token 삭제
        refreshTokenService.deleteRefreshToken(adminId);
    }

    public DashBoardResDto getAdminDashboard(){
        Long totalUsers = userRepository.count();
        Long totalChefs = chefRepository.countAllByApprovalStatus(ApprovalStatus.PENDING);
        Long totalBusinesses = ownerRepository.countAllByApprovalStatus(ApprovalStatus.PENDING);
        Long waitingUsers = totalChefs + totalBusinesses;
        Long waitingLectures = lectureRepository.countAllByApprovalStatus(ApprovalStatus.PENDING);
        Long totalLectures = lectureRepository.count();

        return DashBoardResDto.builder()
                .waitingLectures(waitingLectures)
                .waitingApprovalUsers(waitingUsers)
                .totalLectures(totalLectures)
                .totalUsers(totalUsers)
                .build();
    }
}
