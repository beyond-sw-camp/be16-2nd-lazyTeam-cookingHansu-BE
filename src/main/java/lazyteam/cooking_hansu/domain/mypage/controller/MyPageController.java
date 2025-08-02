package lazyteam.cooking_hansu.domain.mypage.controller;

import lazyteam.cooking_hansu.domain.recipe.dto.MyRecipeListDto;
import lazyteam.cooking_hansu.domain.recipe.service.MyRecipeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/my")
public class MyPageController {

    private final MyRecipeService myRecipeService;

    @GetMapping("/recipes")
    public List<MyRecipeListDto> myRecipeList() {
        return myRecipeService.myRecipeList();
    }
}
