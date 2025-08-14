package lazyteam.cooking_hansu.domain.lecture.controller;


import jakarta.validation.Valid;
import lazyteam.cooking_hansu.domain.lecture.dto.lecture.*;
import lazyteam.cooking_hansu.domain.lecture.service.LectureService;
import lazyteam.cooking_hansu.global.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("lecture")
@RequiredArgsConstructor
//Lecture, CopyOfLectureStep, LectureIngredientsList, LectureVideo 엔티티 서비스
public class LectureController {

    private final LectureService lectureService;

//    강의 등록, 강의 목록 조회, 강의 상세조회, 강의삭제?

//    강의등록

//    메모 : 강의ID > id로 통일시키기

    @PreAuthorize("hasAnyRole('CHEF', 'OWNER')")
    @PostMapping("/post")
    public ResponseEntity<?> create(@Valid @RequestPart LectureCreateDto lectureCreateDto,
                                    @RequestPart List<LectureIngredientsListDto> lectureIngredientsListDto,
                                    @RequestPart List<LectureStepDto> lectureStepDto,
                                    @RequestPart List<LectureVideoDto> lectureVideoDto,
                                    @RequestPart List<MultipartFile> lectureVideoFiles,
                                    @RequestPart MultipartFile multipartFile) {
        UUID lectureId = lectureService.create(lectureCreateDto, lectureIngredientsListDto,lectureStepDto,lectureVideoDto,lectureVideoFiles, multipartFile);

        return new ResponseEntity<>(ResponseDto.ok("강의등록번호 : " + lectureId,HttpStatus.CREATED), HttpStatus.CREATED);
    }

    @PatchMapping("/update/{lectureId}")
    public ResponseEntity<?> lectureUpdate(@Valid @RequestPart LectureUpdateDto lectureUpdateDto,
                                           @PathVariable UUID lectureId,
                                           @RequestPart List<LectureIngredientsListDto> lectureIngredientsListDto,
                                           @RequestPart List<LectureStepDto> lectureStepDto,
                                           @RequestPart List<LectureVideoDto> lectureVideoDto,
                                           @RequestPart List<MultipartFile> lectureVideoFiles,
                                           @RequestPart MultipartFile multipartFile) {
        UUID lectureID = lectureService.update( lectureUpdateDto, lectureId, lectureIngredientsListDto,lectureStepDto,lectureVideoDto,lectureVideoFiles, multipartFile);

        return new ResponseEntity<>(ResponseDto.ok("수정된 강의번호 : " + lectureID,HttpStatus.CREATED), HttpStatus.CREATED);
    }

//    강의 목록조회(delyn 적용, 강의 영상과 재료, 순서까지 일괄 조회되게끔)
    @GetMapping("/list")
    public ResponseEntity<?> lectureFindAll(@PageableDefault(size = 8, sort = "createdAt",
            direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        List<LectureResDto> lectureResDto = lectureService.lectureFindAll(pageable);
        return new ResponseEntity<>(ResponseDto.ok(lectureResDto,HttpStatus.OK),HttpStatus.OK);
    }

//    강의 상세조회
    @GetMapping("/detail/{lectureId}")
    public ResponseEntity<?> lectureFindDetail(@PathVariable UUID lectureId) {
        LectureDetailDto detailDto = lectureService.lectureFindDetail(lectureId);
        return new ResponseEntity<>(ResponseDto.ok(detailDto,HttpStatus.OK),HttpStatus.OK);
    }

//    내 강의 목록 조회
    @PreAuthorize("hasAnyRole('CHEF', 'OWNER')")
    @GetMapping("/mylist")
    public ResponseEntity<?> myLectureFindAll(@PageableDefault(size = 8, sort = "createdAt",
            direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        List<LectureResDto> lectureResDtos = lectureService.myLectureFindAll(pageable);
        return new ResponseEntity<>(ResponseDto.ok(lectureResDtos,HttpStatus.OK),HttpStatus.OK);

    }

//    강의 삭제
    @PreAuthorize("hasAnyRole('CHEF', 'OWNER')")
    @DeleteMapping("/delete/{lectureId}")
    public ResponseEntity<?> lectureDelete(@PathVariable UUID lectureId) {
        lectureService.lectureDelete(lectureId);
        return new ResponseEntity<>(ResponseDto.ok("강의가 삭제되었습니다.", HttpStatus.OK),HttpStatus.OK);
    }

}
