package lazyteam.cooking_hansu.domain.lecture.service;


import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lazyteam.cooking_hansu.domain.lecture.dto.LectureCreateDto;
import lazyteam.cooking_hansu.domain.lecture.dto.LectureIngredientsListDto;
import lazyteam.cooking_hansu.domain.lecture.dto.LectureStepDto;
import lazyteam.cooking_hansu.domain.lecture.entity.Lecture;
import lazyteam.cooking_hansu.domain.lecture.entity.LectureIngredientsList;
import lazyteam.cooking_hansu.domain.lecture.entity.LectureStep;
import lazyteam.cooking_hansu.domain.lecture.repository.LectureIngredientsListRepository;
import lazyteam.cooking_hansu.domain.lecture.repository.LectureRepository;
import lazyteam.cooking_hansu.domain.lecture.repository.LectureStepRepository;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lazyteam.cooking_hansu.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
//Lecture, CopyOfLectureStep, LectureIngredientsList, LectureVideo 엔티티 서비스
public class LectureService {

    private final LectureRepository lectureRepository;
    private final S3Client s3Client;
    private final UserRepository userRepository;
    private final LectureIngredientsListRepository lectureIngredientsListRepository;
    private final LectureStepRepository lectureStepRepository;


    @Value("${cloud.aws.s3.bucket}")
    private String bucket;


    // ====== 강의 등록 ======
    public Long create(LectureCreateDto lectureCreateDto,
                       List<LectureIngredientsListDto> lectureIngredientsListDto,
                       List<LectureStepDto> lectureStepDto,
                       MultipartFile multipartFile) {
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

//        이제 양방향 리스트 저장, dto설계

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
    // ====== 강의 수정 ======
}
