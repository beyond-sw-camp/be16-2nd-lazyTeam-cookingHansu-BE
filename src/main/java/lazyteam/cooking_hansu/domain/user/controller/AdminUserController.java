package lazyteam.cooking_hansu.domain.user.controller;

import jakarta.validation.Valid;
import lazyteam.cooking_hansu.domain.common.dto.RejectRequestDto;
import lazyteam.cooking_hansu.domain.user.dto.UserListDto;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lazyteam.cooking_hansu.domain.user.service.UserService;
import lazyteam.cooking_hansu.global.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/admin/user")
public class AdminUserController {

    private final UserService userService;


//    사용자 승인
    @PatchMapping("/approve/{userId}")
    public ResponseEntity<?> approveUser(@PathVariable UUID userId) {
        userService.approveUser(userId);
        return new ResponseEntity<>(ResponseDto.ok("사용자가 승인되었습니다.", HttpStatus.OK), HttpStatus.OK);
    }

    @PatchMapping("/reject/{userId}")
    public ResponseEntity<?> rejectUser(@PathVariable UUID userId, @Valid @RequestBody RejectRequestDto rejectRequestDto) {
        userService.rejectUser(userId,rejectRequestDto);
        return new ResponseEntity<>(ResponseDto.ok("사용자가 거절되었습니다.", HttpStatus.OK), HttpStatus.OK);
    }

    @GetMapping("/list")
    public ResponseEntity<?> getUserList(@PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<User> userList = userService.getUserList(pageable);
        return new ResponseEntity<>(ResponseDto.ok(userList, HttpStatus.OK), HttpStatus.OK);
    }
}
