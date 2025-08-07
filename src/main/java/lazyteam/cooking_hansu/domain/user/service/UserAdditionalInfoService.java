package lazyteam.cooking_hansu.domain.user.service;

import lazyteam.cooking_hansu.domain.user.dto.request.*;
import lazyteam.cooking_hansu.domain.user.dto.response.*;
import lazyteam.cooking_hansu.domain.user.entity.business.Business;
import lazyteam.cooking_hansu.domain.user.entity.chef.Chef;
import lazyteam.cooking_hansu.domain.user.entity.common.Role;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lazyteam.cooking_hansu.domain.user.repository.BusinessRepository;
import lazyteam.cooking_hansu.domain.user.repository.ChefRepository;
import lazyteam.cooking_hansu.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 회원 추가 정보 입력 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UserAdditionalInfoService {

    private final UserRepository userRepository;
    private final ChefRepository chefRepository;
    private final BusinessRepository businessRepository;

    /**
     * 1단계 추가 정보 입력 (닉네임, 역할 선택)
     */
    public UserAdditionalInfoStep1ResDto updateStep1Info(UUID userId, UserAdditionalInfoStep1RequestDto requestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 1단계 정보 업데이트
        user.updateStep1Info(requestDto.getNickname(), requestDto.getRole());

        // Chef 또는 Owner 역할인 경우 해당 엔티티 생성
        if (requestDto.getRole() == Role.CHEF) {
            Chef chef = Chef.builder()
                    .user(user)
                    .build();
            user.setChef(chef);
        } else if (requestDto.getRole() == Role.OWNER) {
            Business business = Business.builder()
                    .user(user)
                    .build();
            user.setBusiness(business);
        }

        userRepository.save(user);

        return UserAdditionalInfoStep1ResDto.builder()
                .message("1단계 추가 정보가 성공적으로 저장되었습니다.")
                .nickname(user.getNickname())
                .role(user.getRole())
                .isStep1Completed(true)
                .build();
    }

    /**
     * 일반 회원 2단계 추가 정보 입력
     */
    public UserAdditionalInfoStep2ResDto updateGeneralUserStep2Info(UUID userId, GeneralUserStep2RequestDto requestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (user.getRole() != Role.GENERAL) {
            throw new RuntimeException("일반 회원이 아닙니다.");
        }

        user.updateGeneralType(requestDto.getGeneralType());
        userRepository.save(user);

        return UserAdditionalInfoStep2ResDto.builder()
                .message("일반 회원 정보가 성공적으로 저장되었습니다.")
                .isRegistrationCompleted(true)
                .build();
    }

    /**
     * 요식업 종사자 2단계 추가 정보 입력
     */
    public UserAdditionalInfoStep2ResDto updateChefUserStep2Info(UUID userId, ChefUserStep2RequestDto requestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (user.getRole() != Role.CHEF) {
            throw new RuntimeException("요식업 종사자가 아닙니다.");
        }

        Chef chef = user.getChef();
        if (chef == null) {
            throw new RuntimeException("Chef 정보가 존재하지 않습니다.");
        }

        // Chef 정보 업데이트
        chef.updateChefInfo(requestDto.getLicenseNumber(), requestDto.getCuisineType(), requestDto.getLicenseUrl());
        user.completeRegistration();

        userRepository.save(user);

        return UserAdditionalInfoStep2ResDto.builder()
                .message("요식업 종사자 정보가 성공적으로 저장되었습니다.")
                .isRegistrationCompleted(true)
                .build();
    }

    /**
     * 요식업 자영업자 2단계 추가 정보 입력
     */
    public UserAdditionalInfoStep2ResDto updateBusinessUserStep2Info(UUID userId, BusinessUserStep2RequestDto requestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (user.getRole() != Role.OWNER) {
            throw new RuntimeException("요식업 자영업자가 아닙니다.");
        }

        Business business = user.getBusiness();
        if (business == null) {
            throw new RuntimeException("Business 정보가 존재하지 않습니다.");
        }

        // Business 정보 업데이트
        business.updateBusinessInfo(
                requestDto.getBusinessNumber(),
                requestDto.getBusinessUrl(),
                requestDto.getBusinessName(),
                requestDto.getBusinessAddress(),
                requestDto.getShopCategory()
        );
        user.completeRegistration();

        userRepository.save(user);

        return UserAdditionalInfoStep2ResDto.builder()
                .message("요식업 자영업자 정보가 성공적으로 저장되었습니다.")
                .isRegistrationCompleted(true)
                .build();
    }

    /**
     * 사용자 회원가입 상태 확인
     */
    @Transactional(readOnly = true)
    public UserRegistrationStatusResDto getUserRegistrationStatus(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        boolean isStep1Completed = user.getNickname() != null && user.getRole() != null;
        boolean isStep2Completed = !user.isNewUser();

        String nextStepMessage = "";
        if (!isStep1Completed) {
            nextStepMessage = "닉네임과 역할을 선택해주세요.";
        } else if (!isStep2Completed) {
            switch (user.getRole()) {
                case GENERAL:
                    nextStepMessage = "일반 회원 추가 정보를 입력해주세요.";
                    break;
                case CHEF:
                    nextStepMessage = "요식업 종사자 자격증 정보를 입력해주세요.";
                    break;
                case OWNER:
                    nextStepMessage = "사업자 등록 정보를 입력해주세요.";
                    break;
            }
        } else {
            nextStepMessage = "회원가입이 완료되었습니다.";
        }

        return UserRegistrationStatusResDto.builder()
                .isNewUser(user.isNewUser())
                .isStep1Completed(isStep1Completed)
                .isStep2Completed(isStep2Completed)
                .currentRole(user.getRole())
                .nickname(user.getNickname())
                .nextStepMessage(nextStepMessage)
                .build();
    }
}
