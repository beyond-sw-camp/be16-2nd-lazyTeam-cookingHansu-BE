package lazyteam.cooking_hansu.domain.user.service;

import jakarta.persistence.EntityNotFoundException;
import lazyteam.cooking_hansu.domain.user.entity.business.Business;
import lazyteam.cooking_hansu.domain.user.entity.chef.Chef;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lazyteam.cooking_hansu.domain.user.repository.BusinessRepository;
import lazyteam.cooking_hansu.domain.user.repository.ChefRepository;
import lazyteam.cooking_hansu.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * 회원 서비스
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ChefRepository chefRepository;
    private final BusinessRepository businessRepository;

    // TODO: 회원 서비스 메서드 구현 예정

//    강의 승인 메서드
    public void approveUser(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다. userId: " + userId));

        if(user.getRole().equals("CHEF")) {
            Chef chef = chefRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("셰프를 찾을 수 없습니다. userId: " + userId));
            chef.approve();
        } else if(user.getRole().equals("OWNER")) {
            Business business = businessRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("사업자를 찾을 수 없습니다. userId: " + userId));
            business.approve();
        } else {
            throw new IllegalArgumentException("사용자의 역할이 승인 대상이 아닙니다. userId: " + userId);
        }
    }
}
