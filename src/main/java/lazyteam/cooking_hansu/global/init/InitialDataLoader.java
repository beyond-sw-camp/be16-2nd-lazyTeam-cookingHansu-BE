package lazyteam.cooking_hansu.global.init;

import lazyteam.cooking_hansu.domain.admin.entity.Admin;
import lazyteam.cooking_hansu.domain.admin.repository.AdminRepository;
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
    }

}
