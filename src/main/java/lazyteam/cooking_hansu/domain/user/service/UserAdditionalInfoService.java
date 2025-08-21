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
        log.info("회원 추가 정보 입력 시작 - userId: {}, requestDto: {}", userId, requestDto);

        // 요청 데이터 검증
        if (requestDto == null) {
            log.error("요청 데이터가 null입니다 - userId: {}", userId);
            throw new IllegalArgumentException("요청 데이터가 없습니다.");
        }

        if (requestDto.getRole() == null) {
            log.error("역할(role)이 null입니다 - userId: {}, nickname: {}", userId, requestDto.getNickname());
            throw new IllegalArgumentException("역할 선택은 필수입니다.");
        }

        if (requestDto.getNickname() == null || requestDto.getNickname().trim().isEmpty()) {
            log.error("닉네임이 null이거나 비어있습니다 - userId: {}, role: {}", userId, requestDto.getRole());
            throw new IllegalArgumentException("닉네임은 필수입니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("사용자를 찾을 수 없습니다 - userId: {}", userId);
                    return new EntityNotFoundException("사용자를 찾을 수 없습니다.");
                });

        log.info("사용자 조회 성공 - userId: {}, 현재 role: {}", userId, user.getRole());

        // 기본 정보 업데이트 (닉네임, 역할)
        // Chef와 Owner도 바로 해당 역할로 설정 (승인 상태는 별도 관리)
        user.updateStep1Info(requestDto.getNickname(), requestDto.getRole());
        userRepository.save(user);

        log.info("기본 정보 업데이트 완료 - userId: {}, nickname: {}, role: {}",
                userId, requestDto.getNickname(), requestDto.getRole());

        // 역할별 추가 정보 처리
        try {
            switch (requestDto.getRole()) {
                case GENERAL:
                    if (requestDto.getGeneralType() == null) {
                        log.error("일반 회원 유형이 null입니다 - userId: {}", userId);
                        throw new IllegalArgumentException("일반 회원 유형 선택은 필수입니다.");
                    }
                    user.updateGeneralType(requestDto.getGeneralType());
                    user.completeRegistration();
                    log.info("일반 회원 정보 저장 완료 - userId: {}, generalType: {}", userId, requestDto.getGeneralType());
                    break;

                case CHEF:
                    // 자격증 파일이 있는 경우에만 업로드
                    String licenseUrl = null;
                    if (requestDto.getLicenseFile() != null && !requestDto.getLicenseFile().isEmpty()) {
                        licenseUrl = uploadFile(requestDto.getLicenseFile(), "chef/licenses/");
                    } else {
                        // 파일이 없는 경우 임시 URL 또는 기본값 설정
                        licenseUrl = "pending_upload"; // 추후 파일 업로드 대기 상태
                    }

                    ChefDto chefDto = ChefDto.builder()
                            .user(user)
                            .licenseNumber(requestDto.getLicenseNumber())
                            .cuisineType(requestDto.getCuisineType())
                            .licenseUrl(licenseUrl)
                            .build();
                    validateDto(chefDto);
                    Chef chef = chefDto.toEntity();
                    Chef savedChef = chefRepository.save(chef);

                    // 승인 상태를 PENDING으로 설정
                    savedChef.setPending();

                    user.completeRegistration();
                    log.info("셰프 정보 저장 완료 (승인 대기) - userId: {}, licenseNumber: {}, cuisineType: {}, 승인상태: PENDING",
                            userId, requestDto.getLicenseNumber(), requestDto.getCuisineType());
                    break;

                case OWNER:
                    // 사업자등록증 파일이 있는 경우에만 업로드
                    String businessUrl = null;
                    if (requestDto.getBusinessFile() != null && !requestDto.getBusinessFile().isEmpty()) {
                        businessUrl = uploadFile(requestDto.getBusinessFile(), "business/certificates/");
                    } else {
                        // 파일이 없는 경우 임시 URL 또는 기본값 설정
                        businessUrl = "pending_upload"; // 추후 파일 업로드 대기 상태
                    }

                    BusinessDto businessDto = BusinessDto.builder()
                            .user(user)
                            .businessNumber(requestDto.getBusinessNumber())
                            .businessUrl(businessUrl)
                            .businessName(requestDto.getBusinessName())
                            .businessAddress(requestDto.getBusinessAddress())
                            .shopCategory(requestDto.getShopCategory())
                            .build();
                    validateDto(businessDto);
                    Owner owner = businessDto.toEntity();
                    Owner savedOwner = ownerRepository.save(owner);

                    // 승인 상태를 PENDING으로 설정
                    savedOwner.setPending();

                    user.completeRegistration();
                    log.info("사업자 정보 저장 완료 (승인 대기) - userId: {}, businessNumber: {}, businessName: {}, 승인상태: PENDING",
                            userId, requestDto.getBusinessNumber(), requestDto.getBusinessName());
                    break;

                default:
                    log.error("지원하지 않는 역할입니다 - userId: {}, role: {}", userId, requestDto.getRole());
                    throw new IllegalArgumentException("지원하지 않는 역할입니다: " + requestDto.getRole());
            }
        } catch (Exception e) {
            log.error("역할별 정보 처리 중 오류 발생 - userId: {}, role: {}, error: {}",
                    userId, requestDto.getRole(), e.getMessage(), e);
            throw e;
        }

        userRepository.save(user);

        // 응답 메시지 설정 (Chef, Owner의 경우 승인 대기 안내)
        String responseMessage;
        if (requestDto.getRole() == Role.CHEF) {
            responseMessage = "요식업 종사자 정보가 저장되었습니다. 관리자 승인 후 요식업 종사자 권한이 부여됩니다.";
        } else if (requestDto.getRole() == Role.OWNER) {
            responseMessage = "요식업 자영업자 정보가 저장되었습니다. 관리자 승인 후 요식업 자영업자 권한이 부여됩니다.";
        } else {
            responseMessage = "회원 정보가 성공적으로 저장되었습니다.";
        }

        return UserAdditionalInfoResDto.builder()
                .message(responseMessage)
                .nickname(user.getNickname())
                .role(user.getRole()) // 요청한 역할(CHEF, OWNER, GENERAL)이 그대로 설정됨
                .isRegistrationCompleted(!user.isNewUser())
                .build();
    }

    /**
     * 파일을 S3에 업로드하는 메서드
     */
    private String uploadFile(MultipartFile file, String dirName) {
        try {
            // 파일 검증
            validateFile(file);

            // S3에 파일 업로드
            return s3Uploader.upload(file, dirName);
        } catch (Exception e) {
            throw new IllegalArgumentException("파일 업로드에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 파일 유효성 검증
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new EntityNotFoundException("파일이 비어있습니다.");
        }

        // 파일 크기 검증 (20MB 제한)
        long maxFileSize = 20 * 1024 * 1024; // 20MB
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("파일 크기는 20MB를 초과할 수 없습니다.");
        }

        // 파일 확장자 검증
        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("파일명이 유효하지 않습니다.");
        }

        String fileExtension = fileName.toLowerCase();
        if (!fileExtension.endsWith(".jpg") && !fileExtension.endsWith(".jpeg") &&
            !fileExtension.endsWith(".png") && !fileExtension.endsWith(".pdf")) {
            throw new IllegalArgumentException("지원하지 않는 파일 형식입니다. (jpg, jpeg, png, pdf만 허용)");
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
}
