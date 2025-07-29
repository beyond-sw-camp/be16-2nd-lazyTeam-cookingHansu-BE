package lazyteam.cooking_hansu.domain.user.entity.common;

import jakarta.persistence.*;
import lazyteam.cooking_hansu.domain.common.entity.BaseTimeEntity;
import lombok.AllArgsConstructor;
//import lombok.Builder;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 공통 회원 엔티티
 */
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 회원 ID

    private String name; // 이름

    @Enumerated(EnumType.STRING)
    private OauthType oauthType; // 소셜 로그인 유형 (KAKAO, GOOGLE, NAVER)

    private String nickname; // 닉네임

    @Column(unique = true)
    private String email; // 이메일

    private String password; // 비밀번호

    private String profileImageUrl; // 프로필 이미지 URL

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.GENERAL; // 회원 역활 (GENERAL, CHEF, OWNER, BOTH, ADMIN)

    @Enumerated(EnumType.STRING)
    private GeneralType generalType; // 일반 회원 유형 (STUDENT, HOUSEWIFE, LIVINGALONE, ETC)

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private LoginStatus loginStatus = LoginStatus.LOGGED_IN; // 로그인 상태 (LOGGED_IN, LOGGED_OUT, WITHDRAWN, BANNED)

    // 관계 설정은 추후 협의해서 추가 예정
}
