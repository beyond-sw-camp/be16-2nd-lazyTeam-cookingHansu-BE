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

    Optional<User> findByEmail(String email);

    Optional<User> findBySocialId(String socialId);


    // 닉네임 중복 검사
    boolean existsByNickname(String nickname);

    Optional<User> findBySocialIdAndOauthType(String socialId, OauthType oauthType);

    // 탈퇴한 회원 포함하여 조회
    @Query("SELECT u FROM User u WHERE u.socialId = :socialId AND u.oauthType = :oauthType")
    Optional<User> findBySocialIdAndOauthTypeIncludingDeleted(@Param("socialId") String socialId, @Param("oauthType") OauthType oauthType);

    // 탈퇴한 회원만 조회
    Optional<User> findBySocialIdAndOauthTypeAndIsDeleted(String socialId, OauthType oauthType, String isDeleted);
}
