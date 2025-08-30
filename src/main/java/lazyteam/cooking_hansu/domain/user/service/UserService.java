package lazyteam.cooking_hansu.domain.user.service;

import jakarta.persistence.EntityNotFoundException;
import lazyteam.cooking_hansu.domain.common.enums.ApprovalStatus;
import lazyteam.cooking_hansu.domain.common.dto.RejectRequestDto;
import lazyteam.cooking_hansu.domain.notification.dto.SseMessageDto;
import lazyteam.cooking_hansu.domain.notification.entity.TargetType;
import lazyteam.cooking_hansu.domain.notification.service.NotificationService;
import lazyteam.cooking_hansu.domain.user.dto.UserCreateDto;
import lazyteam.cooking_hansu.domain.user.dto.UserListDto;
import lazyteam.cooking_hansu.domain.user.dto.WaitingBusinessListDto;
import lazyteam.cooking_hansu.domain.user.dto.WaitingChefListDto;
import lazyteam.cooking_hansu.domain.user.entity.business.Owner;
import lazyteam.cooking_hansu.domain.user.entity.chef.Chef;
import lazyteam.cooking_hansu.domain.user.entity.common.LoginStatus;
import lazyteam.cooking_hansu.domain.user.entity.common.OauthType;
import lazyteam.cooking_hansu.domain.user.entity.common.Role;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lazyteam.cooking_hansu.domain.user.repository.OwnerRepository;
import lazyteam.cooking_hansu.domain.user.repository.ChefRepository;
import lazyteam.cooking_hansu.domain.user.repository.UserRepository;
import lazyteam.cooking_hansu.global.auth.dto.AuthUtils;
import lazyteam.cooking_hansu.global.service.S3Uploader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
    private final OwnerRepository ownerRepository;
    private final S3Uploader s3Uploader;
    private final NotificationService notificationService;

    @Value("${my.test.user-id}")
    private String testUserIdStr;

    // TODO: 회원 서비스 메서드 구현 예정


//    요리업종 승인 대기 목록 조회
    @Transactional(readOnly = true)
    public Page<WaitingChefListDto> getWaitingChefList(Pageable pageable) {
        Page<Chef> waitingChefs = chefRepository.findAllByApprovalStatus(pageable, ApprovalStatus.PENDING);
        return waitingChefs.map(WaitingChefListDto::fromEntity);
    }

//    사업자 승인 대기 목록 조회
    @Transactional(readOnly = true)
    public Page<WaitingBusinessListDto> getWaitingBusinessList(Pageable pageable) {
        Page<Owner> waitingBusinesses = ownerRepository.findAllByApprovalStatus(pageable, ApprovalStatus.PENDING);
        return waitingBusinesses.map(WaitingBusinessListDto::fromEntity);
    }

//    사용자 승인 - 승인 상태만 변경하고 알림 발송
    public void approveUser(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다. userId: " + userId));

        // Chef 엔티티 확인
        Chef chef = chefRepository.findById(userId).orElse(null);
        if (chef != null) {
            if (chef.getApprovalStatus() == ApprovalStatus.APPROVED) {
                throw new IllegalArgumentException("이미 승인된 셰프입니다. userId: " + userId);
            }
            chef.approve();
            user.updateRoleStatus(Role.CHEF);
            
            // 승인 알림 생성 및 발송
            SseMessageDto approvalNotification = SseMessageDto.builder()
                    .recipientId(userId)
                    .content("셰프 회원가입이 승인되었습니다. 이제 모든 서비스를 이용하실 수 있습니다.")
                    .targetType(TargetType.APPROVAL)
                    .targetId(userId)
                    .build();
            notificationService.createAndDispatch(approvalNotification);
            return;
        }

        // Owner 엔티티 확인
        Owner owner = ownerRepository.findById(userId).orElse(null);
        if (owner != null) {
            if (owner.getApprovalStatus() == ApprovalStatus.APPROVED) {
                throw new IllegalArgumentException("이미 승인된 사업자입니다. userId: " + userId);
            }
            owner.approve();
            user.updateRoleStatus(Role.OWNER);
            
            // 승인 알림 생성 및 발송
            SseMessageDto approvalNotification = SseMessageDto.builder()
                    .recipientId(userId)
                    .content("사업자 회원가입이 승인되었습니다. 이제 모든 서비스를 이용하실 수 있습니다.")
                    .targetType(TargetType.APPROVAL)
                    .targetId(userId)
                    .build();
            notificationService.createAndDispatch(approvalNotification);
            return;
        }

        throw new IllegalArgumentException("승인 대상이 되는 셰프 또는 사업자 정보를 찾을 수 없습니다. userId: " + userId);
    }

//    사용자 승인 거절
    public void rejectUser(UUID userId, RejectRequestDto rejectRequestDto) {
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다. userId: " + userId));

        // Chef 엔티티 확인
        Chef chef = chefRepository.findById(userId).orElse(null);
        if (chef != null) {
            if (chef.getApprovalStatus() == ApprovalStatus.REJECTED) {
                throw new IllegalArgumentException("이미 거절된 셰프입니다. userId: " + userId);
            }
            chef.reject(rejectRequestDto.getReason());
            return;
        }

        // Owner 엔티티 확인
        Owner owner = ownerRepository.findById(userId).orElse(null);
        if (owner != null) {
            if (owner.getApprovalStatus() == ApprovalStatus.REJECTED) {
                throw new IllegalArgumentException("이미 거절된 사업자입니다. userId: " + userId);
            }
            owner.reject(rejectRequestDto.getReason());
            return;
        }

        throw new IllegalArgumentException("거절 대상이 되는 셰프 또는 사업자 정보를 찾을 수 없습니다. userId: " + userId);
    }

//    모든 사용자 조회
    @Transactional(readOnly = true)
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

    // 구글 OAuth 사용자 생성
    public User createGoogleOauth(String sub, String name, String email, String picture, OauthType oauthType) {
        return createOauthUser(sub, name, email, picture, oauthType);
    }

    // 카카오 OAuth 사용자 생성
    public User createKakaoOauth(String sub, String name, String email, String picture, OauthType oauthType) {
        return createOauthUser(sub, name, email, picture, oauthType);
    }

    // 네이버 OAuth 사용자 생성
    public User createNaverOauth(String sub, String name, String email, String picture, OauthType oauthType) {
        return createOauthUser(sub, name, email, picture, oauthType);
    }

    // OAuth 사용자 생성 공통 메서드
    private User createOauthUser(String sub, String name, String email, String picture, OauthType oauthType) {
        String uploadedPictureUrl = null;

        // 소셜 로그인에서 받은 프로필 이미지를 S3에 업로드
        if (picture != null && !picture.isEmpty()) {
            try {
                String fileName = sub; // 소셜 ID를 파일명으로 사용
                uploadedPictureUrl = s3Uploader.uploadFromUrl(
                    picture,
                    "profiles/",
                    fileName
                );
                log.info("프로필 이미지 S3 업로드 성공: {} -> {}", picture, uploadedPictureUrl);
            } catch (Exception e) {
                log.warn("프로필 이미지 S3 업로드 실패, 원본 URL 사용: {}", picture, e);
                uploadedPictureUrl = picture; // 업로드 실패 시 원본 URL 사용
            }
        }

        // 사용자 엔티티 저장
        User user = UserCreateDto.toEntity(sub, name, email, uploadedPictureUrl, oauthType);
        userRepository.save(user);

        return user;
    }

    public User getUserBySocialIdAndOauthType(String email, OauthType oauthType) {
        return userRepository.findBySocialIdAndOauthType(email, oauthType).orElse(null);
    }

    public User getUserById(UUID userId) {
        return userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다. userId: " + userId));
    }

    // 회원탈퇴
    public void deleteUser() {
        UUID userId = AuthUtils.getCurrentUserId();
        User currentUser = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다. userId: " + userId));

        // 프로필 이미지가 S3에 있으면 같이 삭제
        if (currentUser.getPicture() != null) {
            try {
                s3Uploader.delete(currentUser.getPicture());
            } catch (Exception ignore) { }
        }

        currentUser.deleteUser();
    }
}
