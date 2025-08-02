package lazyteam.cooking_hansu.domain.mypage.controller;

import lazyteam.cooking_hansu.domain.mypage.dto.MyLectureListDto;
import lazyteam.cooking_hansu.domain.mypage.dto.MyPostListDto;
import lazyteam.cooking_hansu.domain.mypage.service.MyLectureService;
import lazyteam.cooking_hansu.domain.mypage.service.MyPostService;
import lazyteam.cooking_hansu.domain.mypage.dto.MyRecipeListDto;
import lazyteam.cooking_hansu.domain.mypage.service.MyRecipeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/my")
public class MyPageController {

    private final MyRecipeService myRecipeService;
    private final MyPostService myPostService;
    private final MyLectureService myLectureService;

    @GetMapping("/recipes")
    public List<MyRecipeListDto> myRecipeList() {
        return myRecipeService.myRecipeList();
    }

    @GetMapping("/posts")
    public List<MyPostListDto> myPostList() {
        return myPostService.myPostList();
    }

    @GetMapping("/lectures")
    public List<MyLectureListDto> getMyLectures() {
        return myLectureService.getMyLectures();
    }
}
