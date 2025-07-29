package lazyteam.cooking_hansu.user.repository;

import lazyteam.cooking_hansu.user.domain.common.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 회원 리포지토리
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // TODO: 사용자 정의 메서드 구현 예정
}
