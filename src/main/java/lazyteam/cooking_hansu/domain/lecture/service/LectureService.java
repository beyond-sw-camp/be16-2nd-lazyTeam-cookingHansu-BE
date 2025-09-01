package lazyteam.cooking_hansu.domain.lecture.service;


import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lazyteam.cooking_hansu.domain.interaction.service.InteractionService;
import lazyteam.cooking_hansu.domain.lecture.dto.lecture.*;
import lazyteam.cooking_hansu.domain.lecture.entity.*;
import lazyteam.cooking_hansu.domain.lecture.repository.*;
import lazyteam.cooking_hansu.domain.lecture.util.VideoUtil;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lazyteam.cooking_hansu.domain.user.repository.UserRepository;
import lazyteam.cooking_hansu.domain.interaction.service.RedisInteractionService;
import lazyteam.cooking_hansu.global.auth.dto.AuthUtils;
import lazyteam.cooking_hansu.global.service.S3Uploader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import lazyteam.cooking_hansu.domain.common.enums.ApprovalStatus;
import lazyteam.cooking_hansu.domain.common.dto.RejectRequestDto;
import lazyteam.cooking_hansu.domain.lecture.entity.Lecture;
import lazyteam.cooking_hansu.domain.lecture.entity.LectureVideo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
//Lecture, CopyOfLectureStep, LectureIngredientsList, LectureVideo 엔티티 서비스
public class LectureService {

    private final LectureRepository lectureRepository;
    private final UserRepository userRepository;
    private final LectureIngredientsListRepository lectureIngredientsListRepository;
    private final LectureStepRepository lectureStepRepository;
    private final LectureVideoRepository lectureVideoRepository;
    private final VideoUtil videoUtil;
    private final S3Uploader s3Uploader;
    private final LectureReviewRepository lectureReviewRepository;
    private final RedisInteractionService redisInteractionService; // Redis 서비스 추가
    private final InteractionService interactionService;
    private final LectureProgressRepository progressRepository;

    // ====== 강의 등록 ======
    public UUID create(LectureCreateDto lectureCreateDto,
                       List<LectureIngredientsListDto> lectureIngredientsListDto,
                       List<LectureStepDto> lectureStepDto,
                       List<LectureVideoDto> lectureVideoDto,
                       List<MultipartFile> lectureVideoFiles,
                       MultipartFile multipartFile) {

        UUID userId = AuthUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 사용자입니다."));

        Lecture lecture = lectureRepository.save(lectureCreateDto.toEntity(user));

        // 재료목록 저장
        List<LectureIngredientsList> ingredientsList = lectureIngredientsListDto.stream().map(a -> a.toEntity(lecture)).toList();
        lectureIngredientsListRepository.saveAll(ingredientsList);

        // 재료순서 저장
        List<LectureStep> lectureStepList = lectureStepDto.stream().map(a -> a.toEntity(lecture)).toList();
        lectureStepRepository.saveAll(lectureStepList);

        // [FIX] 영상 DTO가 있을 때만 파일 수 검증 (Index 에러/누락 방지)
        if (lectureVideoDto != null && !lectureVideoDto.isEmpty()) {
            if (lectureVideoFiles == null || lectureVideoFiles.size() != lectureVideoDto.size()) {
                throw new IllegalArgumentException("영상 정보와 파일 수가 일치하지 않습니다.");
            }
        }

        // 강의 영상 자료 저장
        for (int i = 0; i < (lectureVideoDto == null ? 0 : lectureVideoDto.size()); i++) { // [FIX] NPE 방지
            LectureVideoDto dto = lectureVideoDto.get(i);
            MultipartFile file = lectureVideoFiles.get(i);

            try {
                // 파일 확장자 확인
                String ext = videoUtil.detectAndValidateContainerExt(file);
                String fileName = "lecture-" + lecture.getId() + "-video-" + dto.getSequence() + "." + ext;
                String videoUrl = s3Uploader.upload(file, fileName);

                // 영상 길이 추출 (ffprobe 사용) → 초 단위
                log.info("파일 길이 생성");
                int duration = videoUtil.extractDuration(file);

                log.info("파일길이생성완료");
                boolean isPreview = (i == 0);

                LectureVideo video = dto.toEntity(lecture, videoUrl, duration, isPreview);
                lectureVideoRepository.save(video);

            } catch (IOException | InterruptedException e) {
                throw new IllegalArgumentException("강의 영상 업로드 또는 분석 중 오류 발생", e);
            }
        }

        // [FIX] 썸네일: 파일이 있을 때만 처리 (없으면 스킵)
        if (multipartFile != null && !multipartFile.isEmpty()) {
            // 파일 확장자 확인(썸네일)
            String ext = videoUtil.detectAndValidateImageExt(multipartFile);
            String fileName = "lecture-" + lecture.getId() + "-thumbnail." + ext;
            String imageUrl = s3Uploader.upload(multipartFile, fileName);
            lecture.updateImageUrl(imageUrl);
        } else {
            log.info("썸네일 없음(등록 시)");
        }

        return lecture.getId();
    }

    // ====== 강의수정 ======
    public UUID update(LectureUpdateDto lectureUpdateDto,
                       UUID lectureId,
                       List<LectureIngredientsListDto> lectureIngredientsListDto,
                       List<LectureStepDto> lectureStepDto,
                       List<LectureVideoDto> lectureVideoDto,
                       List<MultipartFile> lectureVideoFiles,
                       MultipartFile multipartFile) {

        UUID userId = AuthUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 사용자입니다."));

        Lecture lecture = lectureRepository.findById(lectureId).orElseThrow(() -> new EntityNotFoundException("강의가 없습니다."));

        if (!lecture.getSubmittedBy().getId().equals(user.getId())) {
            throw new EntityNotFoundException("이 강의에 대한 수정 권한이 없습니다.");
        }

        // 강의 정보 수정

        if(lecture.getApprovalStatus().equals(ApprovalStatus.REJECTED)) {
            lecture.setPending();
            log.info("강의상태가 변경되었습니다." + lecture.getApprovalStatus());
        }
        lecture.updateInfo(lectureUpdateDto);



        // 강의 재료 리스트 수정
        if (lectureIngredientsListDto != null && !lectureIngredientsListDto.isEmpty()) { // [FIX] NPE 방지
            lectureIngredientsListRepository.deleteByLecture(lecture);
            List<LectureIngredientsList> ingredientsList = lectureIngredientsListDto.stream()
                    .map(dto -> dto.toEntity(lecture))
                    .toList();
            lectureIngredientsListRepository.saveAll(ingredientsList);
        }
        log.info("재료리스트 수정됐습니다.");

        // 강의 재료순서 수정
        if (lectureStepDto != null && !lectureStepDto.isEmpty()) { // [FIX] NPE 방지
            lectureStepRepository.deleteByLecture(lecture);
            List<LectureStep> lectureStepList = lectureStepDto.stream()
                    .map(dto -> dto.toEntity(lecture))
                    .toList();
            lectureStepRepository.saveAll(lectureStepList);
        }
        log.info("재료순서 수정되었습니다.");

        // 썸네일 수정
        try {
            if (multipartFile != null && !multipartFile.isEmpty()) {
                // [FIX] 먼저 새 파일 검증 → 기존 삭제 (검증 실패 시 기존 썸네일 보존)
                String ext = videoUtil.detectAndValidateImageExt(multipartFile); // [FIX]

                if (lecture.getThumbUrl() != null) {
                    try {
                        s3Uploader.delete(lecture.getThumbUrl());
                    } catch (Exception e) {
                        log.warn("기존 썸네일 삭제 실패(무시): {}", e.getMessage());
                    }
                }

                String thumbnailFileName = "lecture-" + lecture.getId() + "-thumbnail." + ext;
                String thumbnailUrl = s3Uploader.upload(multipartFile, thumbnailFileName);
                lecture.updateImageUrl(thumbnailUrl);
                log.info("썸네일 수정되었습니다. url={}", thumbnailUrl);
            } else {
                log.info("썸네일 변경 없음(파일 미전달)");
            }
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                    e.getMessage(),
                    e
            );
        }


        // ===== 강의영상 수정 =====
        if (lectureVideoDto != null && !lectureVideoDto.isEmpty()) {
            // 1. 기존 영상 제거
            List<LectureVideo> oldVideos = lectureVideoRepository.findByLecture(lecture);
            if (!oldVideos.isEmpty()) {
                for (LectureVideo video : oldVideos) {
                    try {
                        s3Uploader.delete(video.getVideoUrl());
                    } catch (Exception e) {
                        log.warn("기존 강의영상 삭제 실패(무시): url={}, msg={}", video.getVideoUrl(), e.getMessage());
                    }
                }
                lectureVideoRepository.deleteAll(oldVideos);
                log.info("기존 강의영상 제거 완료");
            }

            // 2. 새 영상 등록
            List<LectureVideo> newVideos = new ArrayList<>();

            int fileIndex = 0;

            for (int i = 0; i < lectureVideoDto.size(); i++) {
                LectureVideoDto dto = lectureVideoDto.get(i);
                MultipartFile file = null;

                // 새 파일 필요한 경우에만 fileList에서 꺼냄
                if ((dto.getVideoUrl() == null || dto.getVideoUrl().isBlank())
                        && lectureVideoFiles != null && fileIndex < lectureVideoFiles.size()) {
                    file = lectureVideoFiles.get(fileIndex++);
                }
                String videoUrl;
                int duration;

                if (file != null && !file.isEmpty()) {
                    // 새 파일 업로드
                    String ext = videoUtil.detectAndValidateContainerExt(file);
                    String fileName = "lecture-" + lecture.getId() + "-video-" + dto.getSequence() + "." + ext;

                    log.info("업로드 파일명={}", fileName);
                    videoUrl = s3Uploader.upload(file, fileName);

                    try {
                        duration = videoUtil.extractDuration(file);
                    } catch (IOException | InterruptedException e) {
                        throw new IllegalArgumentException("파일길이 추출 중 오류 발생", e);
                    }
                } else {
                    // 파일이 없고, 기존 URL만 전달된 경우 → 그대로 사용
                    videoUrl = dto.getVideoUrl();
                    duration = dto.getDuration(); // DTO에 기존 길이 담아두는 방식
                }

                boolean isPreview = (i == 0); // 첫 번째 영상만 미리보기
                LectureVideo video = dto.toEntity(lecture, videoUrl, duration, isPreview);
                newVideos.add(video);
            }

            // 3. DB 저장
            lectureVideoRepository.saveAll(newVideos);
            log.info("새 강의영상 등록 완료 (총 {}개)", newVideos.size());
        } else {
            log.info("영상 DTO 미전달 → 교체 스킵");
        }


        return lectureId;
    }


//    강의 목록 조회(승인 안된 강의 목록 조회)
    public Page<WaitingLectureDto> getWaitingLectureList(Pageable pageable){
        Page<Lecture> lectures = lectureRepository.findAllByApprovalStatus(pageable, ApprovalStatus.PENDING);
        return lectures.map(lecture -> WaitingLectureDto.builder()
                .id(lecture.getId())
                .title(lecture.getTitle())
                .description(lecture.getDescription())
                .imageUrl(lecture.getThumbUrl())
                .category(lecture.getCategory())
                .instructorName(lecture.getSubmittedBy().getName())
                .status(lecture.getApprovalStatus())
                .price(lecture.getPrice())
                .duration(lectureVideoRepository.getTotalDurationByLectureId(lecture.getId()))
                .build());
    }

//    강의 승인
    public void approveLecture(UUID lectureId){
        Lecture lecture = lectureRepository.findById(lectureId).orElseThrow(() -> new EntityNotFoundException("강의를 찾을 수 없습니다. lectureId: " + lectureId));
        if(lecture.getApprovalStatus() != null && lecture.getApprovalStatus().equals(ApprovalStatus.APPROVED)) {
            throw new IllegalArgumentException("이미 승인된 강의입니다. lectureId: " + lectureId);
        }
        lecture.approve();
    }

//    강의 거절
    public void rejectLecture(UUID lectureId, RejectRequestDto rejectRequestDto) {
        Lecture lecture = lectureRepository.findById(lectureId).orElseThrow(() -> new EntityNotFoundException("강의를 찾을 수 없습니다. lectureId: " + lectureId));
        if (lecture.getApprovalStatus() != null && lecture.getApprovalStatus().equals(ApprovalStatus.REJECTED)) {
            throw new IllegalArgumentException("이미 거절된 강의입니다. lectureId: " + lectureId);
        }
        lecture.reject(rejectRequestDto.getReason());

    }


// ====== 승인된 강의목록 조회 ======
public Page<LectureResDto> findAllLecture(Pageable pageable) {
    return lectureRepository.findAllByApprovalStatus(pageable,ApprovalStatus.APPROVED)
            .map(LectureResDto::fromEntity);
}



// ====== 내 강의 목록 조회 ======
    public Page<LectureResDto> findAllMyLecture(Pageable pageable) {
        UUID userId = AuthUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 사용자입니다."));

        Page<LectureResDto> myLectures = lectureRepository.findAllBySubmittedById(userId,pageable).map(LectureResDto::fromEntity);
        log.info("판매한 강의목록 : " + myLectures.toString());
        return  myLectures;
    }



// ====== 강의상세목록 조회 ======
public LectureDetailDto findDetailLecture(UUID lectureId) {

    Lecture lecture = lectureRepository.findById(lectureId)
            .orElseThrow(()->new EntityNotFoundException("해당 ID 강의 없습니다."));

    // Redis에서 좋아요 수 가져와서 동기화
    try {
        Long cachedLikeCount = redisInteractionService.getLectureLikesCount(lectureId);
        if (cachedLikeCount != null) {
            // Redis에 캐시된 좋아요 수가 있으면 강의 엔티티에 업데이트
            lecture.updateLikeCount(cachedLikeCount);
            log.debug("강의 좋아요 수 Redis 동기화 - lectureId: {}, count: {}", lectureId, cachedLikeCount);
        } else {
            // Redis에 캐시가 없으면 DB 값을 Redis에 저장
            Long dbLikeCount = (long) (lecture.getLikeCount() != null ? lecture.getLikeCount() : 0);
            redisInteractionService.setLectureLikesCount(lectureId, dbLikeCount);
            log.debug("강의 좋아요 수 Redis 캐싱 - lectureId: {}, count: {}", lectureId, dbLikeCount);
        }
    } catch (Exception e) {
        log.warn("강의 좋아요 수 Redis 동기화 실패 - lectureId: {}", lectureId, e);
        // Redis 연동 실패해도 서비스는 정상 동작하도록 함
    }

    Boolean isLiked = null;
    try {
        UUID currentUserId = AuthUtils.getCurrentUserId();
        if (currentUserId != null) {
            isLiked = interactionService.isLectureLikedByCurrentUser(lectureId);
        }
    } catch (Exception e) {
        log.debug("사용자 좋아요 상태 확인 실패 (비회원 접근): {}", e.getMessage());
    }

    User submittedBy = lecture.getSubmittedBy();
    List<LectureReview> reviews = lectureReviewRepository.findAllByLectureId(lectureId);

    List<LectureQna> qnas = lecture.getQnas();

    List<LectureVideo> videos = lecture.getVideos();

    List<LectureIngredientsList> ingredientsList = lecture.getIngredientsList();

    List<LectureStep> lectureStepList = lecture.getLectureStepList();

    Integer progressPercent = null;
    try {
        UUID userId = AuthUtils.getCurrentUserId();
        int totalVideos = lectureVideoRepository.countByLectureId(lectureId);
        long completedVideos = progressRepository
                .countByUserIdAndLectureVideo_LectureIdAndCompletedTrue(userId, lectureId);

        if (totalVideos > 0) {
            progressPercent = (int) ((completedVideos * 100) / totalVideos);
        } else {
            progressPercent = 0;
        }
    } catch (Exception e) {
        // 로그인 안 된 경우 anonymousUser → progressPercent = null
        progressPercent = null;
    }

    LectureDetailDto lectureDetailDto = LectureDetailDto.fromEntity(lecture,submittedBy,reviews,qnas
            ,videos,ingredientsList,lectureStepList, progressPercent);

    return lectureDetailDto;
}





// ====== 강의삭제 ======
    public void deleteLecture(UUID lectureId) {
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(()-> new EntityNotFoundException("해당 ID 강의 없습니다."));

        lecture.lectureDelete();
    }

    // 영상 진행도 업데이트
    public UUID updateProgress(UUID videoId, int second) {
        UUID userId = AuthUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 사용자입니다."));

        LectureVideo video = lectureVideoRepository.findById(videoId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 강의 영상입니다."));

        LectureProgress progress = progressRepository.findByUserIdAndLectureVideoId(userId, videoId)
                .orElse(LectureProgress.builder()
                        .user(user)
                        .lectureVideo(video)
                        .build());

        progress.updateProgress(second, video.getDuration());
        return progressRepository.save(progress).getId();
    }

    // 강의 단위 진행률(%) 계산 ->향후 필요할 수 있어서 남겨 둠
    public int getLectureProgressPercent(UUID lectureId) {
        UUID userId = AuthUtils.getCurrentUserId();
        int totalVideos = lectureVideoRepository.countByLectureId(lectureId);
        long completedVideos = progressRepository
                .countByUserIdAndLectureVideo_LectureIdAndCompletedTrue(userId, lectureId);

        if (totalVideos == 0) return 0;
        return (int) ((completedVideos * 100) / totalVideos);
    }




}
