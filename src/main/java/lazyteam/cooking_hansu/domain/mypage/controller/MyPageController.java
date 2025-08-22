package lazyteam.cooking_hansu.domain.mypage.controller;

import lazyteam.cooking_hansu.domain.mypage.service.*;
import lazyteam.cooking_hansu.global.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/my")
@Slf4j
public class MyPageController {

    private final MyRecipeService myRecipeService;
    private final MyPostService myPostService;
    private final MyLectureService myLectureService;
    private final MyBookmarkService myBookmarkService;
    private final MyLikeService myLikeService;

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
