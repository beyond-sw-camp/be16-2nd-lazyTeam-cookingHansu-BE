package lazyteam.cooking_hansu.domain.mypage.controller;

import lazyteam.cooking_hansu.domain.mypage.dto.*;
import lazyteam.cooking_hansu.domain.mypage.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/recipes")
    public List<MyRecipeListDto> myRecipeList() {
        return myRecipeService.myRecipeList();
    }

    @GetMapping("/posts")
    public List<MyPostListDto> myPostList() {
        return myPostService.myPostList();
    }

    @GetMapping("/lectures")
    public List<MyLectureListDto> myLectures() {
        return myLectureService.myLectures();
    }

    @GetMapping("/bookmarked-posts")
    public List<MyBookmarkListDto> myBookmarkedPosts() {
        return myBookmarkService.myBookmarkedPosts();
    }

    @GetMapping("/liked-posts")
    public List<MyLikedListDto> myLikedPosts() {
        return myLikeService.myLikedPosts();
    }

}
