package gr.aueb.cf10.gymapp.config;

import gr.aueb.cf10.gymapp.model.User;
import gr.aueb.cf10.gymapp.model.enums.Role;
import gr.aueb.cf10.gymapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminBootstrapRunner implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap.admin.email:}")
    private String adminEmail;

    @Value("${app.bootstrap.admin.password:}")
    private String adminPassword;

    @Value("${app.bootstrap.admin.name:Admin User}")
    private String adminName;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!StringUtils.hasText(adminPassword)) {
            return;
        }

        if (!userRepository.findByRole(Role.ADMIN).isEmpty()) {
            return;
        }

        if (!StringUtils.hasText(adminEmail)) {
            log.warn("ADMIN bootstrap skipped: ADMIN_PASSWORD is set but ADMIN_EMAIL is blank");
            return;
        }

        if (userRepository.existsByEmail(adminEmail)) {
            log.warn("ADMIN bootstrap skipped: email {} already belongs to an existing user", adminEmail);
            return;
        }

        User admin = new User();
        admin.setName(adminName);
        admin.setEmail(adminEmail);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);

        log.info("Created bootstrap ADMIN user with email: {}", adminEmail);
    }
}
