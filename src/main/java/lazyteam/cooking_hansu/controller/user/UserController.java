package lazyteam.cooking_hansu.controller.user;

import lazyteam.cooking_hansu.repository.user.UserRepository;
import lazyteam.cooking_hansu.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;

    // TODO: 회원 관련 API 메서드 구현 예정

}
