package lazyteam.cooking_hansu.domain.admin.controller;

import lazyteam.cooking_hansu.domain.admin.dto.DashBoardResDto;
import lazyteam.cooking_hansu.domain.admin.service.AdminDashBoardService;
import lazyteam.cooking_hansu.global.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/admin")
public class AdminDashBoardController {

    private final AdminDashBoardService adminDashBoardService;

    @GetMapping("/dashboard")
    public ResponseEntity<?> getAdminDashboard() {
        DashBoardResDto adminDashboard = adminDashBoardService.getAdminDashboard();
        return new ResponseEntity<>(ResponseDto.ok(adminDashboard, HttpStatus.OK), HttpStatus.OK);
    }
}
