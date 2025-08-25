package lazyteam.cooking_hansu.domain.admin.service;

import lazyteam.cooking_hansu.domain.admin.dto.DashBoardResDto;
import lazyteam.cooking_hansu.domain.common.enums.ApprovalStatus;
import lazyteam.cooking_hansu.domain.lecture.repository.LectureRepository;
import lazyteam.cooking_hansu.domain.user.repository.OwnerRepository;
import lazyteam.cooking_hansu.domain.user.repository.ChefRepository;
import lazyteam.cooking_hansu.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminDashBoardService {

    private final UserRepository userRepository;
    private final ChefRepository chefRepository;
    private final OwnerRepository ownerRepository;
    private final LectureRepository lectureRepository;

    public DashBoardResDto getAdminDashboard(){
        Long totalUsers = userRepository.count();
        Long totalChefs = chefRepository.countAllByApprovalStatus(ApprovalStatus.PENDING);
        Long totalBusinesses = ownerRepository.countAllByApprovalStatus(ApprovalStatus.PENDING);
        Long waitingUsers = totalChefs + totalBusinesses;
        Long waitingLectures = lectureRepository.countAllByApprovalStatus(ApprovalStatus.PENDING);
        Long totalLectures = lectureRepository.count();

        return DashBoardResDto.builder()
                .waitingLectures(waitingLectures)
                .waitingApprovalUsers(waitingUsers)
                .totalLectures(totalLectures)
                .totalUsers(totalUsers)
                .build();

    }
}
