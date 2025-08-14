package lazyteam.cooking_hansu.domain.lecture.service;


import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lazyteam.cooking_hansu.domain.lecture.dto.lecture.*;
import lazyteam.cooking_hansu.domain.lecture.entity.*;
import lazyteam.cooking_hansu.domain.lecture.repository.*;
import lazyteam.cooking_hansu.domain.lecture.util.VideoUtil;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lazyteam.cooking_hansu.domain.user.repository.UserRepository;
import lazyteam.cooking_hansu.global.service.S3Uploader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import lazyteam.cooking_hansu.domain.common.ApprovalStatus;
import lazyteam.cooking_hansu.domain.common.dto.RejectRequestDto;
import lazyteam.cooking_hansu.domain.lecture.entity.Lecture;
import lazyteam.cooking_hansu.domain.lecture.entity.LectureVideo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;

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

    private final S3Client s3Client;
    private final UserRepository userRepository;
    private final LectureIngredientsListRepository lectureIngredientsListRepository;
    private final LectureStepRepository lectureStepRepository;
    private final LectureVideoRepository lectureVideoRepository;
    private final VideoUtil videoUtil;
    private final S3Uploader s3Uploader;
    private final LectureReviewRepository lectureReviewRepository;
    private final LectureQnaRepository lectureQnaRepository;


    @Value("${cloud.aws.s3.bucket}")
    private String bucket;


    // ====== 강의 등록 ======
    public UUID create(LectureCreateDto lectureCreateDto,
                       List<LectureIngredientsListDto> lectureIngredientsListDto,
                       List<LectureStepDto> lectureStepDto,
                       List<LectureVideoDto> lectureVideoDto,
                       List<MultipartFile> lectureVideoFiles,
                       MultipartFile multipartFile) {

        //  테스트용 이메일 강제 세팅 (로그인 기능 없을 때만 사용!)
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("test@naver.com", null, List.of())
        );

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000000");

        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("유저없음"));
        Lecture lecture = lectureRepository.save(lectureCreateDto.toEntity(user));

//        재료목록 저장
        List<LectureIngredientsList> ingredientsList = lectureIngredientsListDto.stream().map(a -> a.toEntity(lecture)).toList();

            lectureIngredientsListRepository.saveAll(ingredientsList);


//            재료순서 저장
        List<LectureStep> lectureStepList = lectureStepDto.stream().map(a -> a.toEntity(lecture)).toList();

            lectureStepRepository.saveAll(lectureStepList);


//        강의 영상 자료 저장

        for (int i = 0; i < lectureVideoDto.size(); i++) {
            LectureVideoDto dto = lectureVideoDto.get(i);
            MultipartFile file = lectureVideoFiles.get(i);

            try {

                String fileName = "lecture-" + lecture.getId() + "-video-" + dto.getSequence() + ".mp4";
                String videoUrl = s3Uploader.upload(file,fileName);

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


//            강의 썸네일 저장
        MultipartFile image = multipartFile;
        String fileName = "lecture-" + lecture.getId() + "-thumImage-" + image.getOriginalFilename();
        String imageUrl = s3Uploader.upload(image,fileName);
        lecture.updateImageUrl(imageUrl);
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

//        테스트용 UUID 유저 세팅
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000000");
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("유저없음"));

        Lecture lecture = lectureRepository.findById(lectureId).orElseThrow(() -> new EntityNotFoundException("강의가 없습니다."));

        if (!lecture.getSubmittedBy().getId().equals(user.getId())) {
            throw new EntityNotFoundException("이 강의에 대한 수정 권한이 없습니다.");
        }


//        강의 정보 수정
        lecture.updateInfo(lectureUpdateDto);

//        강의 재료 리스트 수정
        if(!lectureIngredientsListDto.isEmpty()) {
            lectureIngredientsListRepository.deleteByLecture(lecture);
            List<LectureIngredientsList> ingredientsList = lectureIngredientsListDto.stream()
                    .map(dto -> dto.toEntity(lecture))
                    .toList();
            lectureIngredientsListRepository.saveAll(ingredientsList);
        }
        log.info("재료리스트 수정됐습니다.");

//        강의 재료순서 수정
        if(!lectureStepDto.isEmpty()) {
            lectureStepRepository.deleteByLecture(lecture);
            List<LectureStep> lectureStepList = lectureStepDto.stream()
                    .map(dto -> dto.toEntity(lecture))
                    .toList();
            lectureStepRepository.saveAll(lectureStepList);
        }
        log.info("재료순서 수정되었습니다.");

        // 썸네일 수정
        try{
            if (multipartFile != null && !multipartFile.isEmpty()) {
                if (lecture.getThumbUrl() != null) {
                    try {
                        s3Uploader.delete(lecture.getThumbUrl());
                    } catch (Exception e) {
                        log.warn("기존 썸네일 삭제 실패(무시): {}", e.getMessage());
                    }
                }

                String thumbnailFileName = "lecture-" + lecture.getId() + "-thumbnail.png";
                String thumbnailUrl = s3Uploader.upload(multipartFile, thumbnailFileName);

                lecture.updateImageUrl(thumbnailUrl);
                log.info("썸네일 수정되었습니다. url={}", thumbnailUrl); // ← 성공시에만
            } else {
                log.info("썸네일 변경 없음(파일 미전달)");
            }
        } catch (IllegalArgumentException e) {
            log.info("썸네일 처리 중 예외(무시): {}", e.getMessage());
        }


//        강의영상  수정

        boolean hasNew =
                (lectureVideoFiles != null && !lectureVideoFiles.isEmpty()) || !lectureVideoDto.isEmpty();

        if (hasNew) {
            if (lectureVideoFiles == null || lectureVideoDto.size() != lectureVideoFiles.size()) {
                throw new IllegalArgumentException("영상 정보와 파일 수가 일치하지 않습니다.");
            }
        }

        if(!lectureVideoDto.isEmpty()) {
            // 기존 영상 제거
            List<LectureVideo> oldVideos = lectureVideoRepository.findByLecture(lecture);

            if (oldVideos.isEmpty()) {
                log.info("기존 강의영상 없음 → 삭제 스킵");
            } else {
                for (LectureVideo video : oldVideos) {
                    try {
                        s3Uploader.delete(video.getVideoUrl());
                    } catch (Exception e) {
                        log.warn("기존 강의영상 삭제 실패(무시): url={}, msg={}", video.getVideoUrl(), e.getMessage());
                    }
                }
                lectureVideoRepository.deleteAll(oldVideos);
                log.info("기존 강의영상 제거했습니다.");
            }
            for (LectureVideo video : oldVideos) {
                s3Uploader.delete(video.getVideoUrl());
            }
            lectureVideoRepository.deleteAll(oldVideos);
            log.info("기존 강의영상 제거했습니다.");

// 새 영상 등록
            List<LectureVideo> newVideos = new ArrayList<>();

            for (int i = 0; i < lectureVideoDto.size(); i++) {
                LectureVideoDto dto = lectureVideoDto.get(i);
                log.info("dto: " + dto.toString());
                MultipartFile file = lectureVideoFiles.get(i);

                log.info(dto.getVideoUrl());
                log.info(file.getOriginalFilename());

                String fileName = "lecture-" + lecture.getId() + "-video-" + dto.getSequence() + ".mp4";
                log.info(fileName);
                String videoUrl = s3Uploader.upload(file, fileName);
                log.info(videoUrl);
                int duration = 0;
                try {
                    log.info("파일길이 추출 시작");
                    duration = videoUtil.extractDuration(file);
                    log.info("파일길이추출완료");
                } catch (IOException | InterruptedException e) {
                    throw new IllegalArgumentException("파일길이 추출 중 오류 발생", e);
                }
                boolean isPreview = (i == 0); // 첫 번째 영상만 미리보기

                LectureVideo video = dto.toEntity(lecture, videoUrl, duration, isPreview);
                newVideos.add(video);
            }
// DB 저장
            lectureVideoRepository.saveAll(newVideos);
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
    public List<LectureResDto> lectureFindAll(Pageable pageable) {
        return lectureRepository.findAll(pageable).stream()
                .filter(a -> a.getApprovalStatus() == ApprovalStatus.APPROVED)
                .map(LectureResDto::fromEntity)
                .toList();
    }



// ====== 내 강의 목록 조회 ======
    public List<LectureResDto> myLectureFindAll(Pageable pageable) {
        //        테스트용 UUID 유저 세팅, 로그인 기능 구현 후 강사 ID를 넣어야 함
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000000");

        return lectureRepository.findAllBySubmittedById(pageable, userId).stream()
                .filter(a -> a.getApprovalStatus() == ApprovalStatus.APPROVED)
                .map(LectureResDto::fromEntity)
                .toList();
    }


    
// ====== 강의상세목록 조회 ======
    public LectureDetailDto lectureFindDetail(UUID lectureId) {
        //        테스트용 UUID 유저 세팅
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000000");
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("유저없음"));

        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(()->new EntityNotFoundException("해당 ID 강의 없습니다."));

        User submittedBy = lecture.getSubmittedBy();
        List<LectureReview> reviews = lectureReviewRepository.findAllByLectureId(lectureId);

        List<LectureQna> qnas = lecture.getQnas();

        List<LectureVideo> videos = lecture.getVideos();

        List<LectureIngredientsList> ingredientsList = lecture.getIngredientsList();

        List<LectureStep> lectureStepList = lecture.getLectureStepList();

        LectureDetailDto lectureDetailDto = LectureDetailDto.fromEntity(lecture,submittedBy,reviews,qnas
                ,videos,ingredientsList,lectureStepList);

        return lectureDetailDto;
    }



// ====== 강의삭제 ======
    public void lectureDelete(UUID lectureId) {
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(()-> new EntityNotFoundException("해당 ID 강의 없습니다."));

        lecture.lectureDelete();
    }

}
