package lazyteam.cooking_hansu.domain.lecture.service;


import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lazyteam.cooking_hansu.domain.lecture.dto.*;
import lazyteam.cooking_hansu.domain.lecture.entity.*;
import lazyteam.cooking_hansu.domain.lecture.repository.LectureIngredientsListRepository;
import lazyteam.cooking_hansu.domain.lecture.repository.LectureRepository;
import lazyteam.cooking_hansu.domain.lecture.repository.LectureStepRepository;
import lazyteam.cooking_hansu.domain.lecture.repository.LectureVideoRepository;
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
import lazyteam.cooking_hansu.domain.lecture.dto.WaitingLectureDto;
import lazyteam.cooking_hansu.domain.lecture.entity.Lecture;
import lazyteam.cooking_hansu.domain.lecture.entity.LectureVideo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.IOException;
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
    // ====== 강의 수정(레디스에 있는지 확인 후 있으면 수정?) ======

//    상세조회에 캐싱처리(레디스에 데이터 있는지 그리고 없으면 rdb조회 후 레디스에 추가 레디스에 ttl..?), 목록조회 페이징 처리


}
