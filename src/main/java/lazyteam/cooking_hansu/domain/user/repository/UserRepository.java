package lazyteam.cooking_hansu.domain.user.repository;

import lazyteam.cooking_hansu.domain.user.entity.common.OauthType;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 회원 리포지토리
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // 닉네임 중복 검사
    boolean existsByNickname(String nickname);

    // 탈퇴한 회원 포함하여 조회
    @Query("SELECT u FROM User u WHERE u.socialId = :socialId AND u.oauthType = :oauthType")
    Optional<User> findBySocialIdAndOauthTypeIncludingDeleted(@Param("socialId") String socialId, @Param("oauthType") OauthType oauthType);

    // 탈퇴한 회원만 조회
    Optional<User> findBySocialIdAndOauthTypeAndIsDeleted(String socialId, OauthType oauthType, String isDeleted);

    // ====== Fetch Join을 활용한 성능 최적화 쿼리 ======
    
    /**
     * User와 연관된 Chef, Owner를 한 번에 조회 (N+1 문제 해결)
     */
    @Query("SELECT u FROM User u " +
           "LEFT JOIN FETCH u.chef " +
           "LEFT JOIN FETCH u.owner " +
           "WHERE u.id = :userId")
    Optional<User> findByIdWithDetails(@Param("userId") UUID userId);
}
