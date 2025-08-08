package lazyteam.cooking_hansu.domain.user.service;

import lazyteam.cooking_hansu.domain.user.dto.request.UserAdditionalInfoRequestDto;
import lazyteam.cooking_hansu.domain.user.dto.response.UserAdditionalInfoResponseDto;
import lazyteam.cooking_hansu.domain.user.dto.response.UserRegistrationStatusResDto;
import lazyteam.cooking_hansu.domain.user.entity.business.Business;
import lazyteam.cooking_hansu.domain.user.entity.chef.Chef;
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
     * 회원 추가 정보 입력 (통합)
     */
    public UserAdditionalInfoResponseDto updateAdditionalInfo(UUID userId, UserAdditionalInfoRequestDto requestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 기본 정보 업데이트 (닉네임, 역할)
        user.updateStep1Info(requestDto.getNickname(), requestDto.getRole());

        // 역할별 추가 정보 처리
        switch (requestDto.getRole()) {
            case GENERAL:
                if (requestDto.getGeneralType() == null) {
                    throw new RuntimeException("일반 회원 유형 선택은 필수입니다.");
                }
                user.updateGeneralType(requestDto.getGeneralType());
                break;

            case CHEF:
                validateChefData(requestDto);
                Chef chef = Chef.builder()
                        .user(user)
                        .licenseNumber(requestDto.getLicenseNumber())
                        .cuisineType(requestDto.getCuisineType())
                        .licenseUrl(requestDto.getLicenseUrl())
                        .build();
                user.setChef(chef);
                user.completeRegistration();
                break;

            case OWNER:
                validateBusinessData(requestDto);
                Business business = Business.builder()
                        .user(user)
                        .businessNumber(requestDto.getBusinessNumber())
                        .businessUrl(requestDto.getBusinessUrl())
                        .businessName(requestDto.getBusinessName())
                        .businessAddress(requestDto.getBusinessAddress())
                        .shopCategory(requestDto.getShopCategory())
                        .build();
                user.setBusiness(business);
                user.completeRegistration();
                break;

            default:
                throw new RuntimeException("지원하지 않는 역할입니다.");
        }

        userRepository.save(user);

        return UserAdditionalInfoResponseDto.builder()
                .message("회원 정보가 성공적으로 저장되었습니다.")
                .nickname(user.getNickname())
                .role(user.getRole())
                .isRegistrationCompleted(!user.isNewUser())
                .build();
    }

    /**
     * Chef 데이터 유효성 검증
     */
    private void validateChefData(UserAdditionalInfoRequestDto requestDto) {
        if (requestDto.getLicenseNumber() == null || requestDto.getLicenseNumber().trim().isEmpty()) {
            throw new RuntimeException("자격 번호는 필수입니다.");
        }
        if (requestDto.getCuisineType() == null) {
            throw new RuntimeException("자격 업종 선택은 필수입니다.");
        }
        if (requestDto.getLicenseUrl() == null || requestDto.getLicenseUrl().trim().isEmpty()) {
            throw new RuntimeException("자격증 이미지 URL은 필수입니다.");
        }
    }

    /**
     * Business 데이터 유효성 검증
     */
    private void validateBusinessData(UserAdditionalInfoRequestDto requestDto) {
        if (requestDto.getBusinessNumber() == null || requestDto.getBusinessNumber().trim().isEmpty()) {
            throw new RuntimeException("사업자 등록 번호는 필수입니다.");
        }
        if (requestDto.getBusinessUrl() == null || requestDto.getBusinessUrl().trim().isEmpty()) {
            throw new RuntimeException("사업자 등록증 파일 URL은 필수입니다.");
        }
        if (requestDto.getBusinessName() == null || requestDto.getBusinessName().trim().isEmpty()) {
            throw new RuntimeException("상호명은 필수입니다.");
        }
        if (requestDto.getBusinessAddress() == null || requestDto.getBusinessAddress().trim().isEmpty()) {
            throw new RuntimeException("사업지 주소는 필수입니다.");
        }
        if (requestDto.getShopCategory() == null || requestDto.getShopCategory().trim().isEmpty()) {
            throw new RuntimeException("사업 업종은 필수입니다.");
        }
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
