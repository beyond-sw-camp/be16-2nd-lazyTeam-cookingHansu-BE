package lazyteam.cooking_hansu.domain.user.service;

import jakarta.persistence.EntityNotFoundException;
import lazyteam.cooking_hansu.domain.common.ApprovalStatus;
import lazyteam.cooking_hansu.domain.common.dto.RejectRequestDto;
import lazyteam.cooking_hansu.domain.user.dto.CommonProfileDto;
import lazyteam.cooking_hansu.domain.user.dto.UserListDto;
import lazyteam.cooking_hansu.domain.user.dto.WaitingBusinessListDto;
import lazyteam.cooking_hansu.domain.user.dto.WaitingChefListDto;
import lazyteam.cooking_hansu.domain.user.entity.business.Business;
import lazyteam.cooking_hansu.domain.user.entity.chef.Chef;
import lazyteam.cooking_hansu.domain.user.entity.common.LoginStatus;
import lazyteam.cooking_hansu.domain.user.entity.common.OauthType;
import lazyteam.cooking_hansu.domain.user.entity.common.Role;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lazyteam.cooking_hansu.domain.user.repository.BusinessRepository;
import lazyteam.cooking_hansu.domain.user.repository.ChefRepository;
import lazyteam.cooking_hansu.domain.user.repository.UserRepository;
import lazyteam.cooking_hansu.global.service.S3Uploader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 회원 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final ChefRepository chefRepository;
    private final BusinessRepository businessRepository;
    private final S3Uploader s3Uploader;

    // TODO: 회원 서비스 메서드 구현 예정


//    요리업종 승인 대기 목록 조회
    public Page<WaitingChefListDto> getWaitingChefList(Pageable pageable) {
        Page<Chef> waitingChefs = chefRepository.findAllByApprovalStatus(pageable, ApprovalStatus.PENDING);
        return waitingChefs.map(WaitingChefListDto::fromEntity);
    }

//    사업자 승인 대기 목록 조회
    public Page<WaitingBusinessListDto> getWaitingBusinessList(Pageable pageable) {
        Page<Business> waitingBusinesses = businessRepository.findAllByApprovalStatus(pageable, ApprovalStatus.PENDING);
        return waitingBusinesses.map(WaitingBusinessListDto::fromEntity);
    }

//    사용자 승인
    public void approveUser(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다. userId: " + userId));

        if(user.getRole() == Role.CHEF) {
            Chef chef = chefRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("셰프를 찾을 수 없습니다. userId: " + userId));
            if (chef.getApprovalStatus() == ApprovalStatus.APPROVED) {
                throw new IllegalArgumentException("이미 승인된 셰프입니다. userId: " + userId);
            }
            chef.approve();
        } else if(user.getRole() == Role.OWNER) {
            Business business = businessRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("사업자를 찾을 수 없습니다. userId: " + userId));
            if (business.getApprovalStatus() == ApprovalStatus.APPROVED) {
                throw new IllegalArgumentException("이미 승인된 사업자입니다. userId: " + userId);
            }
            business.approve();
        } else {
            throw new IllegalArgumentException("사용자의 역할이 승인 대상이 아닙니다. userId: " + userId);
        }
    }

//    사용자 승인 거절
    public void rejectUser(UUID userId, RejectRequestDto rejectRequestDto) {
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다. userId: " + userId));

        if(user.getRole() == Role.CHEF) {
            Chef chef = chefRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("셰프를 찾을 수 없습니다. userId: " + userId));
            if (chef.getApprovalStatus() == ApprovalStatus.REJECTED) {
                throw new IllegalArgumentException("이미 거절된 셰프입니다. userId: " + userId);
            }
            chef.reject(rejectRequestDto.getReason());
        } else if(user.getRole() == Role.OWNER) {
            Business business = businessRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("사업자를 찾을 수 없습니다. userId: " + userId));
            if (business.getApprovalStatus() == ApprovalStatus.REJECTED) {
                throw new IllegalArgumentException("이미 거절된 사업자입니다. userId: " + userId);
            }
            business.reject(rejectRequestDto.getReason());
        } else {
            throw new IllegalArgumentException("사용자의 역할이 거절 대상이 아닙니다. userId: " + userId);
        }
    }

//    모든 사용자 조회
    public Page<UserListDto> getUserList(Pageable pageable) {
        Page<User> userPage = userRepository.findAll(pageable);
        return userPage.map(UserListDto::fromEntity);
    }

//    사용자 활성화
    public void activateUser(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다. userId: " + userId));
        if(user.getLoginStatus() == LoginStatus.ACTIVE) {
            throw new IllegalArgumentException("이미 활성화된 사용자입니다. userId: " + userId);
        }
        user.updateStatus(LoginStatus.ACTIVE);
    }

//    사용자 비활성화
    public void inactiveUser(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다. userId: " + userId));
        if(user.getLoginStatus() == LoginStatus.INACTIVE) {
            throw new IllegalArgumentException("이미 비활성화된 사용자입니다. userId: " + userId);
        }
        user.updateStatus(LoginStatus.INACTIVE);
    }

    public User getUserBySocialId(String socialId) {
        return userRepository.findBySocialId(socialId).orElse(null);
    }

    // 사용자 생성 (OAuth 로그인용)
    public User createOauth(CommonProfileDto dto, OauthType oauthType) {
        String uploadedPictureUrl = null;

        // 소셜 로그인에서 받은 프로필 이미지를 S3에 업로드
        if (dto.getPicture() != null && !dto.getPicture().isEmpty()) {
            try {
                String fileName = "profile-" + dto.getSub();
                uploadedPictureUrl = s3Uploader.uploadFromUrl(
                    dto.getPicture(),
                    "profiles/",
                    fileName
                );
                log.info("프로필 이미지 S3 업로드 성공: {} -> {}", dto.getPicture(), uploadedPictureUrl);
            } catch (Exception e) {
                log.warn("프로필 이미지 S3 업로드 실패, 원본 URL 사용: {}", dto.getPicture(), e);
                uploadedPictureUrl = dto.getPicture(); // 업로드 실패 시 원본 URL 사용
            }
        }

        User user = User.builder()
                .email(dto.getEmail())
                .name(dto.getName())
                .oauthType(oauthType)
                .socialId(dto.getSub())
                .picture(uploadedPictureUrl)
                .build();
        userRepository.save(user);
        return user;
    }

    /**
     * 기존 사용자의 프로필 이미지를 S3에 업로드하여 업데이트
     * @param user 업데이트할 사용자
     * @param newPictureUrl 새로운 프로필 이미지 URL
     * @return 업데이트된 S3 URL
     */
    public String updateUserProfileImage(User user, String newPictureUrl) {
        if (newPictureUrl == null || newPictureUrl.isEmpty()) {
            return null;
        }

        try {
            // 기존 S3 이미지 삭제 (S3 URL인 경우에만)
            if (user.getPicture() != null && user.getPicture().contains("amazonaws.com")) {
                try {
                    s3Uploader.delete(user.getPicture());
                    log.info("기존 프로필 이미지 S3 삭제 성공: {}", user.getPicture());
                } catch (Exception e) {
                    log.warn("기존 프로필 이미지 S3 삭제 실패: {}", user.getPicture(), e);
                }
            }

            // 새 이미지 S3 업로드
            String fileName = "profile-" + user.getSocialId();
            String uploadedUrl = s3Uploader.uploadFromUrl(
                newPictureUrl,
                "profiles/",
                fileName
            );

            // 사용자 프로필 이미지 URL 업데이트
            user.updateAdditionalInfo(user.getName(), user.getNickname(), uploadedUrl);
            userRepository.save(user);

            log.info("사용자 프로필 이미지 업데이트 성공: {} -> {}", newPictureUrl, uploadedUrl);
            return uploadedUrl;

        } catch (Exception e) {
            log.error("프로필 이미지 업데이트 실패: {}", newPictureUrl, e);
            throw new IllegalArgumentException("프로필 이미지 업데이트 실패", e);
        }
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다. email: " + email));
    }
}
