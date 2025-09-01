package lazyteam.cooking_hansu.domain.user.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lazyteam.cooking_hansu.domain.user.dto.response.BusinessDto;
import lazyteam.cooking_hansu.domain.user.dto.response.ChefDto;
import lazyteam.cooking_hansu.domain.user.dto.request.UserAdditionalInfoRequestDto;
import lazyteam.cooking_hansu.domain.user.dto.response.UserAdditionalInfoResDto;
import lazyteam.cooking_hansu.domain.user.entity.business.Owner;
import lazyteam.cooking_hansu.domain.user.entity.chef.Chef;
import lazyteam.cooking_hansu.domain.user.entity.common.Role;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lazyteam.cooking_hansu.domain.user.entity.common.GeneralType;
import lazyteam.cooking_hansu.domain.user.repository.OwnerRepository;
import lazyteam.cooking_hansu.domain.user.repository.ChefRepository;
import lazyteam.cooking_hansu.domain.user.repository.UserRepository;
import lazyteam.cooking_hansu.global.service.S3Uploader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 회원 추가 정보 입력 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserAdditionalInfoService {

    private final UserRepository userRepository;
    private final ChefRepository chefRepository;
    private final OwnerRepository ownerRepository;
    private final S3Uploader s3Uploader;
    private final Validator validator;

    /**
     * 회원 추가 정보 입력 (통합)
     */
    public UserAdditionalInfoResDto updateAdditionalInfo(UUID userId, UserAdditionalInfoRequestDto requestDto) {
        log.info("회원 추가 정보 입력 시작 - userId: {}, role: {}", userId, requestDto.getRole());

        // 기본 검증
        validateBasicRequest(requestDto);

        // 사용자 조회
        User user = findUserById(userId);

        // 기본 정보 업데이트
        // Chef나 Owner로 신청한 경우 role을 GENERAL로, generalType을 ETC로 강제 설정
        Role finalRole = requestDto.getRole();
        GeneralType finalGeneralType = requestDto.getGeneralType();
        
        if (requestDto.getRole() == Role.CHEF || requestDto.getRole() == Role.OWNER) {
            finalRole = Role.GENERAL;
            finalGeneralType = GeneralType.ETC;
        }
        
        user.updateStep1Info(requestDto.getNickname(), finalRole, finalGeneralType);

        // 역할별 추가 정보 처리
        processRoleSpecificInfo(user, requestDto);

        // 회원가입 완료 처리
        user.completeRegistration();

        userRepository.save(user);

        log.info("회원 추가 정보 입력 완료 - userId: {}, role: {}", userId, requestDto.getRole());

        return createResponse(user, requestDto.getRole());
    }

    /**
     * 기본 요청 검증
     */
    private void validateBasicRequest(UserAdditionalInfoRequestDto requestDto) {
        if (requestDto == null) {
            throw new IllegalArgumentException("요청 데이터가 없습니다.");
        }
        if (requestDto.getRole() == null) {
            throw new IllegalArgumentException("역할 선택은 필수입니다.");
        }
        if (requestDto.getNickname() == null || requestDto.getNickname().trim().isEmpty()) {
            throw new IllegalArgumentException("닉네임은 필수입니다.");
        }
    }

    /**
     * 사용자 조회
     */
    private User findUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
    }

    /**
     * 역할별 추가 정보 처리
     */
    private void processRoleSpecificInfo(User user, UserAdditionalInfoRequestDto requestDto) {
        switch (requestDto.getRole()) {
            case GENERAL -> processGeneralUser(user, requestDto);
            case CHEF -> processChefUser(user, requestDto);
            case OWNER -> processOwnerUser(user, requestDto);
            default -> throw new IllegalArgumentException("지원하지 않는 역할입니다: " + requestDto.getRole());
        }
    }

    /**
     * 일반 회원 처리
     */
    private void processGeneralUser(User user, UserAdditionalInfoRequestDto requestDto) {
        if (requestDto.getGeneralType() == null) {
            throw new IllegalArgumentException("일반 회원 유형 선택은 필수입니다.");
        }
        user.updateGeneralType(requestDto.getGeneralType());
        log.info("일반 회원 정보 저장 완료 - userId: {}, generalType: {}",
                user.getId(), requestDto.getGeneralType());
    }

    /**
     * 셰프 회원 처리
     */
    private void processChefUser(User user, UserAdditionalInfoRequestDto requestDto) {
        // 필수 정보 검증
        validateChefRequest(requestDto);

        // 자격증 파일 업로드
        String licenseUrl = uploadFileIfPresent(requestDto.getLicenseFile(), "chef/licenses/");

        // Chef 엔티티 직접 생성 및 저장
        Chef chef = Chef.builder()
                .user(user)
                .licenseNumber(requestDto.getLicenseNumber())
                .cuisineType(requestDto.getCuisineType())
                .licenseUrl(licenseUrl)
                .build();

        Chef savedChef = chefRepository.save(chef);
        savedChef.setPending(); // 승인 대기 상태로 설정

        log.info("셰프 정보 저장 완료 (승인 대기) - userId: {}, licenseNumber: {}",
                user.getId(), requestDto.getLicenseNumber());
    }

    /**
     * 사업자 회원 처리
     */
    private void processOwnerUser(User user, UserAdditionalInfoRequestDto requestDto) {
        // 필수 정보 검증
        validateOwnerRequest(requestDto);

        // 사업자등록증 파일 업로드
        String businessUrl = uploadFileIfPresent(requestDto.getBusinessFile(), "business/certificates/");

        // Owner 엔티티 직접 생성 및 저장
        Owner owner = Owner.builder()
                .user(user)
                .businessNumber(requestDto.getBusinessNumber())
                .businessUrl(businessUrl)
                .businessName(requestDto.getBusinessName())
                .businessAddress(requestDto.getBusinessAddress())
                .shopCategory(requestDto.getShopCategory())
                .build();

        Owner savedOwner = ownerRepository.save(owner);
        savedOwner.setPending(); // 승인 대기 상태로 설정

        log.info("사업자 정보 저장 완료 (승인 대기) - userId: {}, businessNumber: {}",
                user.getId(), requestDto.getBusinessNumber());
    }

    /**
     * 셰프 요청 검증
     */
    private void validateChefRequest(UserAdditionalInfoRequestDto requestDto) {
        if (requestDto.getLicenseNumber() == null || requestDto.getLicenseNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("자격 번호는 필수입니다.");
        }
        if (requestDto.getCuisineType() == null) {
            throw new IllegalArgumentException("자격 업종 선택은 필수입니다.");
        }
    }

    /**
     * 사업자 요청 검증
     */
    private void validateOwnerRequest(UserAdditionalInfoRequestDto requestDto) {
        if (requestDto.getBusinessNumber() == null || requestDto.getBusinessNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("사업자 등록 번호는 필수입니다.");
        }
        if (requestDto.getBusinessName() == null || requestDto.getBusinessName().trim().isEmpty()) {
            throw new IllegalArgumentException("상호명은 필수입니다.");
        }
        if (requestDto.getBusinessAddress() == null || requestDto.getBusinessAddress().trim().isEmpty()) {
            throw new IllegalArgumentException("사업지 주소는 필수입니다.");
        }
        if (requestDto.getShopCategory() == null || requestDto.getShopCategory().trim().isEmpty()) {
            throw new IllegalArgumentException("사업 업종은 필수입니다.");
        }
    }

    /**
     * 파일이 있는 경우에만 업로드
     */
    private String uploadFileIfPresent(MultipartFile file, String dirName) {
        try {
            if (file == null || file.isEmpty()) {
                log.error("파일이 비어었거나 존재하지 않습니다. - dirName: {}", dirName);
                throw new EntityNotFoundException("파일이 비어있거나 존재하지 않습니다.");
            }
            return s3Uploader.upload(file, dirName);
        } catch (Exception e) {
            log.error("파일 업로드 실패 - fileName: {}, error: {}", file.getOriginalFilename(), e.getMessage());
            throw new IllegalArgumentException("파일 업로드에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * DTO 유효성 검증
     */
    private void validateDto(Object dto) {
        Set<ConstraintViolation<Object>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            String errorMessages = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            throw new RuntimeException(errorMessages);
        }
    }

    /**
     * 응답 생성
     */
    private UserAdditionalInfoResDto createResponse(User user, Role role) {
        String responseMessage = switch (role) {
            case CHEF -> "요식업 종사자 정보가 저장되었습니다. 관리자 승인 후 요식업 종사자 권한이 부여됩니다.";
            case OWNER -> "요식업 자영업자 정보가 저장되었습니다. 관리자 승인 후 요식업 자영업자 권한이 부여됩니다.";
            case GENERAL -> "회원 정보가 성공적으로 저장되었습니다.";
            default -> "회원 정보가 저장되었습니다.";
        };

        return UserAdditionalInfoResDto.builder()
                .message(responseMessage)
                .nickname(user.getNickname())
                .role(user.getRole())
                .isRegistrationCompleted(!user.isNewUser())
                .build();
    }
}
