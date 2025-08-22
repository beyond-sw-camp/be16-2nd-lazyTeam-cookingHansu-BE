package lazyteam.cooking_hansu.domain.user.controller;

import lazyteam.cooking_hansu.domain.user.repository.UserRepository;
import lazyteam.cooking_hansu.domain.user.service.UserService;
import lazyteam.cooking_hansu.global.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;

    // TODO: 회원 관련 API 메서드 구현 예정

    // 회원 탈퇴
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteUser() {
        userService.deleteUser();
        return new ResponseEntity<>(
                ResponseDto.ok("회원 탈퇴가 완료되었습니다.", HttpStatus.OK),
                HttpStatus.OK
        );
    }
}
