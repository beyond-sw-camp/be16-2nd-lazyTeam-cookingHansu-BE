package lazyteam.cooking_hansu.domain.lecture.controller;


import jakarta.validation.Valid;
import lazyteam.cooking_hansu.domain.lecture.dto.*;
import lazyteam.cooking_hansu.domain.lecture.entity.LectureVideo;
import lazyteam.cooking_hansu.domain.lecture.service.LectureReviewService;
import lazyteam.cooking_hansu.domain.lecture.service.LectureService;
import lazyteam.cooking_hansu.global.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("lecture")
@RequiredArgsConstructor
//Lecture, CopyOfLectureStep, LectureIngredientsList, LectureVideo 엔티티 서비스
public class LectureController {

    private final LectureService lectureService;

//    강의 등록, 강의 목록 조회, 강의 상세조회, 강의삭제?

//    강의등록

//    메모 : 강의ID > id로 통일시키기

    @PreAuthorize("hasAnyRole('CHEF', 'OWNER', 'BOTH')")
    @PostMapping("/post")
    public ResponseEntity<?> create(@Valid @RequestPart LectureCreateDto lectureCreateDto,
                                    @RequestPart List<LectureIngredientsListDto> lectureIngredientsListDto,
                                    @RequestPart List<LectureStepDto> lectureStepDto,
                                    @RequestPart List<LectureVideoDto> lectureVideoDto,
                                    @RequestPart List<MultipartFile> lectureVideoFiles,
                                    @RequestPart MultipartFile multipartFile) {
        Long lectureId = lectureService.create(lectureCreateDto, lectureIngredientsListDto,lectureStepDto,lectureVideoDto,lectureVideoFiles, multipartFile);

        return new ResponseEntity<>(ResponseDto.ok("강의등록번호 : " + lectureId,HttpStatus.CREATED), HttpStatus.CREATED);
    }

//    @PostMapping("/update")
//    public ResponseEntity<?> lectureUpdate(@Valid @RequestBody LectureUpdateDto lectureUpdateDto) {
//        lectureService.lectureUpdate();
//        return null;
//    }



}
