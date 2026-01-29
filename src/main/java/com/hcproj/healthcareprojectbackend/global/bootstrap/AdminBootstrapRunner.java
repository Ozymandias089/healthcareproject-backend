package com.hcproj.healthcareprojectbackend.global.bootstrap;

import com.hcproj.healthcareprojectbackend.auth.entity.UserEntity;
import com.hcproj.healthcareprojectbackend.auth.entity.UserRole;
import com.hcproj.healthcareprojectbackend.auth.repository.UserRepository;
import com.hcproj.healthcareprojectbackend.global.config.properties.BootstrapAdminProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Profile({"local", "dev"})
@RequiredArgsConstructor
public class AdminBootstrapRunner implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final BootstrapAdminProperties props;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!props.isEnabled()) return;

        if (userRepository.existsByRole(UserRole.ADMIN)) return;

        String email = require(props.getEmail(), "BOOTSTRAP_ADMIN_EMAIL");
        String handle = defaultIfBlank(props.getHandle(), "admin");
        String nickname = defaultIfBlank(props.getNickname(), "관리자");
        String profileImageUrl = props.getProfileImageUrl();

        String passwordHash = passwordEncoder.encode(
                require(props.getPassword(), "BOOTSTRAP_ADMIN_PASSWORD")
        );

        UserEntity admin = UserEntity.registerAdmin(
                email,
                handle,
                passwordHash,
                nickname,
                profileImageUrl
        );

        userRepository.save(admin);
        log.info("[BOOTSTRAP] Admin account created: {}", email);
    }

    private static String require(String v, String name) {
        if (v == null || v.isBlank()) {
            throw new IllegalStateException("Missing required bootstrap config: " + name);
        }
        return v;
    }

    private static String defaultIfBlank(String v, String dft) {
        return (v == null || v.isBlank()) ? dft : v;
    }
}
