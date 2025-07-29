package lazyteam.cooking_hansu.domain.user.entity.common;

import jakarta.persistence.*;
import lazyteam.cooking_hansu.domain.common.entity.BaseTimeEntity;
import lazyteam.cooking_hansu.domain.user.entity.business.Business;
import lazyteam.cooking_hansu.domain.user.entity.chef.Chef;
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
    @Column(name = "user_id")
    private Long id; // 회원 ID

    @Column(nullable = false)
    private String name; // 이름

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OauthType oauthType; // 소셜 로그인 유형 (KAKAO, GOOGLE, NAVER)

    @Column(nullable = false)
    private String nickname; // 닉네임

    @Column(unique = true)
    private String email; // 이메일

    @Column(nullable = false)
    private String password; // 비밀번호

    @Column(nullable = false)
    private String profileImageUrl; // 프로필 이미지 URL

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.GENERAL; // 회원 역활 (GENERAL, CHEF, OWNER, BOTH, ADMIN)

    @Enumerated(EnumType.STRING)
    private GeneralType generalType; // 일반 회원 유형 (STUDENT, HOUSEWIFE, LIVINGALONE, ETC)

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private LoginStatus loginStatus = LoginStatus.LOGGED_IN; // 로그인 상태 (LOGGED_IN, LOGGED_OUT, WITHDRAWN, BANNED)

    // 관계 설정은 추후 협의해서 추가 예정
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Chef chef;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Business business;
}
