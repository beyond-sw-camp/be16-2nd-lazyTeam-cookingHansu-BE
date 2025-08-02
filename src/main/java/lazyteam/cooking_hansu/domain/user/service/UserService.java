package lazyteam.cooking_hansu.domain.user.service;

import jakarta.persistence.EntityNotFoundException;
import lazyteam.cooking_hansu.domain.common.dto.RejectRequestDto;
import lazyteam.cooking_hansu.domain.user.dto.UserListDto;
import lazyteam.cooking_hansu.domain.user.entity.business.Business;
import lazyteam.cooking_hansu.domain.user.entity.chef.Chef;
import lazyteam.cooking_hansu.domain.user.entity.common.LoginStatus;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lazyteam.cooking_hansu.domain.user.repository.BusinessRepository;
import lazyteam.cooking_hansu.domain.user.repository.ChefRepository;
import lazyteam.cooking_hansu.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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
public class UserService {

    private final UserRepository userRepository;
    private final ChefRepository chefRepository;
    private final BusinessRepository businessRepository;

    // TODO: 회원 서비스 메서드 구현 예정

//    사용자 승인
    public void approveUser(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다. userId: " + userId));

        if(user.getRole().equals("CHEF")) {
            Chef chef = chefRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("셰프를 찾을 수 없습니다. userId: " + userId));
            chef.approve();
        } else if(user.getRole().equals("OWNER")) {
            Business business = businessRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("사업자를 찾을 수 없습니다. userId: " + userId));
            business.approve();
        } else {
            throw new IllegalArgumentException("사용자의 역할이 승인 대상이 아닙니다. userId: " + userId);
        }
    }

//    사용자 승인 거절
    public void rejectUser(UUID userId, RejectRequestDto rejectRequestDto) {
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다. userId: " + userId));

        if(user.getRole().equals("CHEF")) {
            Chef chef = chefRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("셰프를 찾을 수 없습니다. userId: " + userId));
            chef.reject(rejectRequestDto.getReason());
        } else if(user.getRole().equals("OWNER")) {
            Business business = businessRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("사업자를 찾을 수 없습니다. userId: " + userId));
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
        if(user.getLoginStatus().equals(LoginStatus.ACTIVE)) {
            throw new IllegalArgumentException("이미 활성화된 사용자입니다. userId: " + userId);
        }
        user.updateStatus(LoginStatus.INACTIVE);
    }

//    사용자 비활성화
    public void inactiveUser(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다. userId: " + userId));
        if(user.getLoginStatus().equals(LoginStatus.INACTIVE)) {
            throw new IllegalArgumentException("이미 비활성화된 사용자입니다. userId: " + userId);
        }
        user.updateStatus(LoginStatus.INACTIVE);
    }
}
