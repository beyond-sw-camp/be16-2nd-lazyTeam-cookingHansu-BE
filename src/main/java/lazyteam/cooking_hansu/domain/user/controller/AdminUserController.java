package lazyteam.cooking_hansu.domain.user.controller;

import lazyteam.cooking_hansu.domain.user.service.UserService;
import lazyteam.cooking_hansu.global.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/admin/user")
public class AdminUserController {

    private final UserService userService;

    public ResponseEntity<?> approveUser(UUID userId) {
        userService.approveUser(userId);
        return new ResponseEntity<>(ResponseDto.ok("사용자가 승인되었습니다.", HttpStatus.OK), HttpStatus.OK);
    }
}
