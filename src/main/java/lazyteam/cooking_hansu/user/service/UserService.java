package lazyteam.cooking_hansu.user.service;

import lazyteam.cooking_hansu.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 회원 서비스
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // TODO: 회원 서비스 메서드 구현 예정
}
