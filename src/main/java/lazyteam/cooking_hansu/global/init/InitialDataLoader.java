package lazyteam.cooking_hansu.global.init;

import lazyteam.cooking_hansu.domain.admin.entity.Admin;
import lazyteam.cooking_hansu.domain.admin.repository.AdminRepository;
import lazyteam.cooking_hansu.domain.user.entity.common.User;
import lazyteam.cooking_hansu.domain.user.entity.common.Role;
import lazyteam.cooking_hansu.domain.user.entity.common.OauthType;
import lazyteam.cooking_hansu.domain.user.entity.common.GeneralType;
import lazyteam.cooking_hansu.domain.user.entity.common.LoginStatus;
import lazyteam.cooking_hansu.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

// CommandLineRunner 인터페이스를 구현함으로써 해당 컴포넌트가 스프링빈으로 등록되는 시점에 run에서도 자동실행.

@Component
@RequiredArgsConstructor
public class InitialDataLoader implements CommandLineRunner {
    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;
    @Value("${app.admin.name}")
    private String adminName;
    @Value("${app.admin.password}")
    private String adminPassword;

    @Override
    public void run(String... args) throws Exception {
        // 관리자 초기 데이터
        if(adminRepository.findByEmail("admin@naver.com").isEmpty()){
            Admin admin = Admin.builder()
                    .email(adminEmail)
                    .name(adminName)
                    .password(passwordEncoder.encode(adminPassword))
                    .build();
            adminRepository.save(admin);
        }

        // 테스트 회원 초기 데이터 생성
        createTestUsers();
    }

    private void createTestUsers() {
        // 기존 테스트 회원이 있는지 확인
        if (userRepository.findByEmail("testuser1@test.com").isPresent()) {
            return; // 이미 테스트 유저가 있으면 생성하지 않음
        }

        // 테스트 회원 1 - 일반 사용자 (학생)
        User testUser1 = User.builder()
                .name("김테스트1")
                .nickname("테스트유저1")
                .email("testuser1@test.com")
                .password(passwordEncoder.encode("testpass123"))
                .profileImageUrl("https://via.placeholder.com/150/1")
                .role(Role.GENERAL)
                .generalType(GeneralType.STUDENT)
                .oauthType(null)
                .loginStatus(LoginStatus.ACTIVE)
                .build();

        // 테스트 회원 2 - 일반 사용자 (주부)
        User testUser2 = User.builder()
                .name("이테스트2")
                .nickname("테스트유저2")
                .email("testuser2@test.com")
                .password(passwordEncoder.encode("testpass123"))
                .profileImageUrl("https://via.placeholder.com/150/2")
                .role(Role.GENERAL)
                .generalType(GeneralType.HOUSEWIFE)
                .oauthType(null)
                .loginStatus(LoginStatus.ACTIVE)
                .build();

        // 테스트 회원 3 - 일반 사용자 (자취생)
        User testUser3 = User.builder()
                .name("박테스트3")
                .nickname("테스트유저3")
                .email("testuser3@test.com")
                .password(passwordEncoder.encode("testpass123"))
                .profileImageUrl("https://via.placeholder.com/150/3")
                .role(Role.GENERAL)
                .generalType(GeneralType.LIVINGALONE)
                .oauthType(null)
                .loginStatus(LoginStatus.ACTIVE)
                .build();

        // 테스트 회원 4 - 요식업 종사자
        User testUser4 = User.builder()
                .name("최테스트4")
                .nickname("테스트셰프4")
                .email("testuser4@test.com")
                .password(passwordEncoder.encode("testpass123"))
                .profileImageUrl("https://via.placeholder.com/150/4")
                .role(Role.CHEF)
                .generalType(null)
                .oauthType(null)
                .loginStatus(LoginStatus.ACTIVE)
                .build();

        // 테스트 회원 5 - 자영업자
        User testUser5 = User.builder()
                .name("정테스트5")
                .nickname("테스트사장5")
                .email("testuser5@test.com")
                .password(passwordEncoder.encode("testpass123"))
                .profileImageUrl("https://via.placeholder.com/150/5")
                .role(Role.OWNER)
                .generalType(null)
                .oauthType(null)
                .loginStatus(LoginStatus.ACTIVE)
                .build();

        // 데이터베이스에 저장
        userRepository.save(testUser1);
        userRepository.save(testUser2);
        userRepository.save(testUser3);
        userRepository.save(testUser4);
        userRepository.save(testUser5);

        System.out.println("===============================");
        System.out.println("🧪 테스트 회원 5명 생성 완료!");
        System.out.println("===============================");
        System.out.println("1. " + testUser1.getNickname() + " (" + testUser1.getEmail() + ") - " + testUser1.getRole());
        System.out.println("2. " + testUser2.getNickname() + " (" + testUser2.getEmail() + ") - " + testUser2.getRole());
        System.out.println("3. " + testUser3.getNickname() + " (" + testUser3.getEmail() + ") - " + testUser3.getRole());
        System.out.println("4. " + testUser4.getNickname() + " (" + testUser4.getEmail() + ") - " + testUser4.getRole());
        System.out.println("5. " + testUser5.getNickname() + " (" + testUser5.getEmail() + ") - " + testUser5.getRole());
        System.out.println("===============================");
    }

}
