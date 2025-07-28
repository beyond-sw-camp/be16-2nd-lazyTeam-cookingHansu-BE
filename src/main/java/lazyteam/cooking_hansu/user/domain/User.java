package lazyteam.cooking_hansu.user.domain;

import jakarta.persistence.*;
import lazyteam.cooking_hansu.domain.common.BaseTimeEntity;
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

    private String name;

    @Enumerated(EnumType.STRING)
    private OauthType oauthType;

    private String nickname;

    @Column(unique = true)
    private String email;

    private String password;

    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.GENERAL;

    @Enumerated(EnumType.STRING)
    private GeneralType generalType;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private LoginStatus loginStatus = LoginStatus.ACTIVE;

    // 관계 설정은 추후 협의해서 추가 예정
}
