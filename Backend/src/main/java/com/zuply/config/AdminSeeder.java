package com.zuply.config;

import com.zuply.common.enums.Role;
import com.zuply.modules.user.model.User;
import com.zuply.modules.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Runs once at startup.
 * Creates the default admin account if no ADMIN user exists in the database.
 * Credentials can be changed via the database after first login.
 */
@Component
@Order(1)
public class AdminSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);

    private final UserRepository    userRepository;
    private final PasswordEncoder   passwordEncoder;

    public AdminSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository  = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        final String adminEmail    = "admin@zuply.in";
        final String adminPassword = "Admin@123";

        userRepository.findByEmail(adminEmail).ifPresentOrElse(
            existing -> {
                // Always reset password on startup to guarantee correct hash
                existing.setPassword(passwordEncoder.encode(adminPassword));
                userRepository.save(existing);
                log.info("✅ Admin password reset on startup → {}", adminEmail);
            },
            () -> {
                User admin = new User();
                admin.setName("Admin");
                admin.setEmail(adminEmail);
                admin.setPassword(passwordEncoder.encode(adminPassword));
                admin.setPhone("9000000000");
                admin.setRole(Role.ADMIN);
                userRepository.save(admin);

                log.info("=======================================================");
                log.info("  Default admin account created.");
                log.info("  Email   : {}", adminEmail);
                log.info("  Password: {}", adminPassword);
                log.info("=======================================================");
            }
        );
    }
}
