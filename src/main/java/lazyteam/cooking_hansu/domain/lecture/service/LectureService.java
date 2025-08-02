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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.List;

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


    @Value("${cloud.aws.s3.bucket}")
    private String bucket;


    // ====== 강의 등록 ======
    public Long create(LectureCreateDto lectureCreateDto,
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

        User user = userRepository.findByEmail(email).orElseThrow(()->new EntityNotFoundException("유저없음"));
        Lecture lecture = lectureRepository.save(lectureCreateDto.toEntity(user));

//        재료목록 저장
            List<LectureIngredientsList> ingredientsList = lectureIngredientsListDto.stream().map(a->a.toEntity(lecture)).toList();
            for(LectureIngredientsList ingredients : ingredientsList) {
                lectureIngredientsListRepository.save(ingredients);
            }

//            재료순서 저장
        List<LectureStep> lectureStepList = lectureStepDto.stream().map(a->a.toEntity(lecture)).toList();
        for(LectureStep steps : lectureStepList) {
            lectureStepRepository.save(steps);
        }
        System.out.println("재료순서");
//        강의 영상 자료 저장

        for (int i = 0; i < lectureVideoDto.size(); i++) {
            LectureVideoDto dto = lectureVideoDto.get(i);
            MultipartFile file = lectureVideoFiles.get(i);

            try {
                log.info("파일이름생성");
                String fileName = "lecture-" + lecture.getLectureId() + "-video-" + dto.getSequence() + ".mp4";


                log.info("업로드요청 생성");
                PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(fileName)
                        .contentType(file.getContentType())
                        .build();

                log.info("파일업로드 시작");
                s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));
                log.info("파일업로드 성공");

                log.info("s3파일 url생성");
                String videoUrl = s3Client.utilities()
                        .getUrl(builder -> builder.bucket(bucket).key(fileName))
                        .toExternalForm();

                log.info("파일 url생성성공");

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
        if(image != null) {

            String fileName = "lecture-" + lecture.getLectureId() + "-thumImage-" + image.getOriginalFilename();
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileName)
                    .contentType(image.getContentType())
                    .build();
            try {s3Client.putObject(putObjectRequest, RequestBody.fromBytes(image.getBytes()));}
            catch (Exception e) {throw new IllegalArgumentException("이미지 업로드 실패");}

            String imgUrl = s3Client.utilities().getUrl(a->a.bucket(bucket).key(fileName)).toExternalForm();
            lecture.updateImageUrl(imgUrl);

        }
        return lecture.getLectureId();

    }
    // ====== 강의 수정(레디스에 있는지 확인 후 있으면 수정?) ======

//    상세조회에 캐싱처리(레디스에 데이터 있는지 그리고 없으면 rdb조회 후 레디스에 추가 레디스에 ttl..?), 목록조회 페이징 처리


}
