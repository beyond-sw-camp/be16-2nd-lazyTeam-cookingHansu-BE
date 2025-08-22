package lazyteam.cooking_hansu.domain.user.repository;

import lazyteam.cooking_hansu.domain.user.entity.common.OauthType;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

/**
 * 회원 리포지토리
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    
    // 닉네임 중복 검사
    boolean existsByNickname(String nickname);
    
    // 닉네임으로 사용자 조회
    Optional<User> findByNickname(String nickname);
}
