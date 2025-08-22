package lazyteam.cooking_hansu.domain.user.controller;

import jakarta.validation.Valid;
import lazyteam.cooking_hansu.domain.common.dto.RejectRequestDto;
import lazyteam.cooking_hansu.domain.user.dto.UserListDto;
import lazyteam.cooking_hansu.domain.user.dto.WaitingBusinessListDto;
import lazyteam.cooking_hansu.domain.user.dto.WaitingChefListDto;
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


//    사용자 승인 대기 목록 조회(요리업종)
    @GetMapping("/waiting/chef")
    public ResponseEntity<?> getWaitingChefList(@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<WaitingChefListDto> waitingChefList = userService.getWaitingChefList(pageable);
        return new ResponseEntity<>(ResponseDto.ok(waitingChefList, HttpStatus.OK), HttpStatus.OK);
    }

    @GetMapping("/waiting/business")
    public ResponseEntity<?> getWaitingBusinessList(@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<WaitingBusinessListDto> waitingBusinessList = userService.getWaitingBusinessList(pageable);
        return new ResponseEntity<>(ResponseDto.ok(waitingBusinessList, HttpStatus.OK), HttpStatus.OK);
    }

//    사용자 승인
    @PatchMapping("/approve/{userId}")
    public ResponseEntity<?> approveUser(@PathVariable UUID userId) {
        userService.approveUser(userId);
        return new ResponseEntity<>(ResponseDto.ok("사용자가 승인되었습니다.", HttpStatus.OK), HttpStatus.OK);
    }

//    사용자 거절
    @PatchMapping("/reject/{userId}")
    public ResponseEntity<?> rejectUser(@PathVariable UUID userId, @Valid @RequestBody RejectRequestDto rejectRequestDto) {
        userService.rejectUser(userId,rejectRequestDto);
        return new ResponseEntity<>(ResponseDto.ok("사용자가 거절되었습니다.", HttpStatus.OK), HttpStatus.OK);
    }


//    사용자 전체 목록 조회
    @GetMapping("/list")
    public ResponseEntity<?> getUserList(@PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<UserListDto> userList = userService.getUserList(pageable);
        return new ResponseEntity<>(ResponseDto.ok(userList, HttpStatus.OK), HttpStatus.OK);
    }

//    사용자 활성화
    @PatchMapping("/active/{userId}")
    public ResponseEntity<?> activateUser(@PathVariable UUID userId){
        userService.activateUser(userId);
        return new ResponseEntity<>(ResponseDto.ok("사용자를 활성화 하였습니다.", HttpStatus.OK), HttpStatus.OK);
    }

//    사용자 비활성화
    @PatchMapping("/inactive/{userId}")
    public ResponseEntity<?> inactiveUser(@PathVariable  UUID userId){
        userService.inactiveUser(userId);
        return new ResponseEntity<>(ResponseDto.ok("사용자를 비활성화 하였습니다.", HttpStatus.OK), HttpStatus.OK);
    }
}
