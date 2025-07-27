package lazyteam.cooking_hansu.repository.user;

import lazyteam.cooking_hansu.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 회원 리포지토리
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // TODO: 사용자 정의 메서드 구현 예정
}
