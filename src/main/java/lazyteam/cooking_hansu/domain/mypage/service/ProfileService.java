package lazyteam.cooking_hansu.domain.mypage.service;

import lazyteam.cooking_hansu.domain.mypage.dto.ProfileResponseDto;
import lazyteam.cooking_hansu.domain.mypage.dto.ProfileUpdateRequestDto;
import lazyteam.cooking_hansu.domain.user.entity.common.Role;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lazyteam.cooking_hansu.domain.user.repository.UserRepository;
import lazyteam.cooking_hansu.global.service.S3Uploader;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ProfileService {

    private final UserRepository userRepository;
    private final S3Uploader s3Uploader;

    @Value("${my.test.user-id}")
    private String testUserIdStr;

    // 프로필 조회
    public ProfileResponseDto getProfile() {
        User user = getCurrentUser();
        
        return ProfileResponseDto.builder()
                .nickname(user.getNickname())
                .email(user.getEmail())
                .info(user.getInfo())
                .profileImageUrl(user.getProfileImageUrl())
                .userType(getUserTypeDisplayName(user.getRole()))
                .build();
    }

    // 프로필 수정
    public ProfileResponseDto updateProfile(ProfileUpdateRequestDto requestDto) {
        User user = getCurrentUser();
        
        // 닉네임 중복 검사 (자신의 닉네임이 아닌 경우)
        if (!user.getNickname().equals(requestDto.getNickname()) &&
            userRepository.existsByNickname(requestDto.getNickname())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }
        
        // 프로필 정보 업데이트
        user.updateProfile(
                requestDto.getNickname(),
                requestDto.getInfo(),
                requestDto.getProfileImageUrl()
        );
        
        return getProfile();
    }

    // 프로필 이미지 업로드
    public String uploadProfileImage(MultipartFile image) {
        System.out.println("=== 이미지 업로드 시작 ===");
        System.out.println("파일명: " + image.getOriginalFilename());
        System.out.println("파일 크기: " + image.getSize() + " bytes");
        
        User user = getCurrentUser();
        
        // 기존 이미지가 있다면 S3에서 삭제
        if (user.getProfileImageUrl() != null) {
            try {
                s3Uploader.delete(user.getProfileImageUrl());
                System.out.println("기존 이미지 삭제 완료");
            } catch (Exception e) {
                System.out.println("기존 이미지 삭제 실패: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // 새 이미지 업로드
        System.out.println("새 이미지 S3 업로드 시작");
        try {
            String imageUrl = s3Uploader.upload(image, "profile-images/");
            System.out.println("S3 업로드 완료: " + imageUrl);
            
            // 사용자 프로필 이미지 URL 업데이트
            user.updateProfileImage(imageUrl);
            System.out.println("사용자 프로필 이미지 업데이트 완료");
            return imageUrl;
        } catch (Exception e) {
            System.out.println("S3 업로드 실패: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    // 현재 로그인한 사용자 조회 (테스트용)
    private User getCurrentUser() {
        UUID testUserId = UUID.fromString(testUserIdStr);
        return userRepository.findById(testUserId)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다."));
    }
    
    // 역할명을 한글로 변환하는 메서드
    private String getUserTypeDisplayName(Role role) {
        switch (role) {
            case GENERAL:
                return "일반 사용자";
            case CHEF:
                return "요리사";
            case OWNER:
                return "사업자";
            default:
                return "사용자";
        }
    }


}
