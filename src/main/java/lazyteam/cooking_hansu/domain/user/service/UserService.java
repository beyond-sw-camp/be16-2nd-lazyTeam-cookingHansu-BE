package lazyteam.cooking_hansu.domain.user.service;

import jakarta.persistence.EntityNotFoundException;
import lazyteam.cooking_hansu.domain.common.ApprovalStatus;
import lazyteam.cooking_hansu.domain.common.dto.RejectRequestDto;
import lazyteam.cooking_hansu.domain.user.dto.UserListDto;
import lazyteam.cooking_hansu.domain.user.dto.WaitingBusinessListDto;
import lazyteam.cooking_hansu.domain.user.dto.WaitingChefListDto;
import lazyteam.cooking_hansu.domain.user.entity.business.Business;
import lazyteam.cooking_hansu.domain.user.entity.chef.Chef;
import lazyteam.cooking_hansu.domain.user.entity.common.LoginStatus;
import lazyteam.cooking_hansu.domain.user.entity.common.Role;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lazyteam.cooking_hansu.domain.user.repository.BusinessRepository;
import lazyteam.cooking_hansu.domain.user.repository.ChefRepository;
import lazyteam.cooking_hansu.domain.user.repository.UserRepository;
import lazyteam.cooking_hansu.global.dto.ResponseDto;
import lazyteam.cooking_hansu.global.service.S3Uploader;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * 회원 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final ChefRepository chefRepository;
    private final BusinessRepository businessRepository;
    private final S3Uploader s3Uploader;

    @Value("${my.test.user-id}")
    private String testUserIdStr;

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


    // 로그인 전 테스트 요
    private User getCurrentUser() {
        UUID testUserId = UUID.fromString(testUserIdStr);
        return userRepository.findById(testUserId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
    }

    // 회원탈퇴
    public void deleteUser() {
        User currentUser = getCurrentUser();

        // 프로필 이미지가 S3에 있으면 같이 삭제
        if (currentUser.getProfileImageUrl() != null) {
            try {
                s3Uploader.delete(currentUser.getProfileImageUrl());
            } catch (Exception ignore) { }
        }

        currentUser.deleteUser();
    }
}
