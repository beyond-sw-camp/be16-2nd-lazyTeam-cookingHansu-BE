package lazyteam.cooking_hansu.domain.mypage.controller;

import lazyteam.cooking_hansu.domain.mypage.dto.*;
import lazyteam.cooking_hansu.domain.mypage.service.*;
import lazyteam.cooking_hansu.global.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/my")
public class MyPageController {

    private final MyRecipeService myRecipeService;
    private final MyPostService myPostService;
    private final MyLectureService myLectureService;
    private final MyBookmarkService myBookmarkService;
    private final MyLikeService myLikeService;
    private final ProfileService profileService;

    // 프로필 조회
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        return new ResponseEntity<>(
                ResponseDto.ok(profileService.getProfile(), HttpStatus.OK),
                HttpStatus.OK
        );
    }

    // 프로필 수정
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody ProfileUpdateRequestDto requestDto) {
        return new ResponseEntity<>(
                ResponseDto.ok(profileService.updateProfile(requestDto), HttpStatus.OK),
                HttpStatus.OK
        );
    }

    // 프로필 이미지 업로드
    @PostMapping("/profile/image")
    public ResponseEntity<?> uploadProfileImage(@RequestParam("image") MultipartFile image) {
        return new ResponseEntity<>(
                ResponseDto.ok(profileService.uploadProfileImage(image), HttpStatus.OK),
                HttpStatus.OK
        );
    }


    @GetMapping("/recipes")
    public ResponseEntity<?> myRecipeList() {
        return new ResponseEntity<>(
                ResponseDto.ok(myRecipeService.myRecipeList(), HttpStatus.OK),
                HttpStatus.OK
        );
    }

    @GetMapping("/posts")
    public ResponseEntity<?> myPostList() {
        return new ResponseEntity<>(
                ResponseDto.ok(myPostService.myPostList(), HttpStatus.OK),
                HttpStatus.OK
        );
    }

    @GetMapping("/lectures")
    public ResponseEntity<?> myLectures(@PageableDefault(size = 8) Pageable pageable) {
        return new ResponseEntity<>(
                ResponseDto.ok(myLectureService.myLectures(pageable), HttpStatus.OK),
                HttpStatus.OK
        );
    }

    @GetMapping("/bookmarked-posts")
    public ResponseEntity<?> myBookmarkedPosts() {
        return new ResponseEntity<>(
                ResponseDto.ok(myBookmarkService.myBookmarkedPosts(), HttpStatus.OK),
                HttpStatus.OK
        );
    }

    @GetMapping("/liked-posts")
    public ResponseEntity<?> myLikedPosts() {
        return new ResponseEntity<>(
                ResponseDto.ok(myLikeService.myLikedPosts(), HttpStatus.OK),
                HttpStatus.OK
        );
    }
}
