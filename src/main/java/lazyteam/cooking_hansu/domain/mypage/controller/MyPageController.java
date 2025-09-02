package lazyteam.cooking_hansu.domain.mypage.controller;

import lazyteam.cooking_hansu.domain.mypage.dto.MyLectureListDto;
import lazyteam.cooking_hansu.domain.mypage.dto.ProfileUpdateRequestDto;
import lazyteam.cooking_hansu.domain.mypage.service.*;
import lazyteam.cooking_hansu.global.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/my")
@Slf4j
public class MyPageController {

    private final MyPageService myPageService;

    // 프로필 조회
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        return new ResponseEntity<>(
                ResponseDto.ok(myPageService.getProfile(), HttpStatus.OK),
                HttpStatus.OK
        );
    }

    // 프로필 수정
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody ProfileUpdateRequestDto requestDto) {
        return new ResponseEntity<>(
                ResponseDto.ok(myPageService.updateProfile(requestDto), HttpStatus.OK),
                HttpStatus.OK
        );
    }

    // 프로필 이미지 업로드
    @PostMapping("/profile/image")
    public ResponseEntity<?> uploadProfileImage(@RequestParam("image") MultipartFile image) {
        return new ResponseEntity<>(
                ResponseDto.ok(myPageService.uploadProfileImage(image), HttpStatus.OK),
                HttpStatus.OK
        );
    }

    @GetMapping("/posts")
    public ResponseEntity<?> myPostList() {
        return new ResponseEntity<>(
                ResponseDto.ok(myPageService.getMyPosts(), HttpStatus.OK),
                HttpStatus.OK
        );
    }

    //   내가 구매한 강의 목록조회
    @GetMapping("/lectures")
    public ResponseEntity<?> myLectures(@PageableDefault(size = 6, sort = "createdAt",
            direction = Sort.Direction.DESC) Pageable pageable) {
        Page<MyLectureListDto> lectureResDto = myPageService.getMyLectures(pageable);
        return new ResponseEntity<>(ResponseDto.ok(lectureResDto,HttpStatus.OK),HttpStatus.OK);
    }


    @GetMapping("/bookmarked-posts")
    public ResponseEntity<?> myBookmarkedPosts() {
        return new ResponseEntity<>(
                ResponseDto.ok(myPageService.getMyBookmarks(), HttpStatus.OK),
                HttpStatus.OK
        );
    }

    @GetMapping("/liked-posts")
    public ResponseEntity<?> myLikedPosts() {
        return new ResponseEntity<>(
                ResponseDto.ok(myPageService.getMyLikes(), HttpStatus.OK),
                HttpStatus.OK
        );
    }

    @GetMapping("/liked-lectures")
    public ResponseEntity<?> myLikedLectures() {
        return new ResponseEntity<>(
                ResponseDto.ok(myPageService.getMyLikedLectures(), HttpStatus.OK),
                HttpStatus.OK
        );
    }

}
