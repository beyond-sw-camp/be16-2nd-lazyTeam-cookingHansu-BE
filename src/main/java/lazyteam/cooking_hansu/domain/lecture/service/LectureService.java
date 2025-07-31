package lazyteam.cooking_hansu.domain.lecture.service;


import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lazyteam.cooking_hansu.domain.lecture.dto.LectureCreateDto;
import lazyteam.cooking_hansu.domain.lecture.entity.Lecture;
import lazyteam.cooking_hansu.domain.lecture.repository.LectureRepository;
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

@Service
@Transactional
@RequiredArgsConstructor
//Lecture, CopyOfLectureStep, LectureIngredientsList, LectureVideo 엔티티 서비스
public class LectureService {

    private final LectureRepository lectureRepository;
    public final S3Client s3Client;
    public final UserRepository userRepository;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;


    // ====== 강의 등록 ======
    public Long create(LectureCreateDto lectureCreateDto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
//        User repo에서 findByEmail 미 구현
        User user = userRepository.findByEmail(email).orElseThrow(()->new EntityNotFoundException("유저없음"));
        Lecture lecture = lectureRepository.save(lectureCreateDto.toEntity(user));

        MultipartFile image = lectureCreateDto.getImageFile();
        if(image != null) {

            String fileName = "lecture-" + lecture.getLectureId() + "-thumImage-" + image.getOriginalFilename();
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileName)
                    .contentType(image.getContentType())
                    .build();
            try {

                s3Client.putObject(putObjectRequest, RequestBody.fromBytes(image.getBytes()));
            }
            catch (Exception e) {
                throw new IllegalArgumentException("이미지 업로드 실패");
            }

            String imgUrl = s3Client.utilities().getUrl(a->a.bucket(bucket).key(fileName)).toExternalForm();
            lecture.updateImageUrl(imgUrl);

        }
        return lecture.getLectureId();

        // ====== 강의 수정 ======






    }
}
