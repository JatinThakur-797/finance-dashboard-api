package com.auth.config;

import com.auth.entities.Role;
import com.auth.entities.User;
import com.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.admin.name}")
    private String adminName;

    @Bean
    public CommandLineRunner initAdmin() {
        return args -> {

            if (userRepository.findByEmail(adminEmail).isEmpty()) {

                BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

                User admin = new User();
                admin.setEmail(adminEmail);
                admin.setName(adminName);
                admin.setPasswordHash(encoder.encode(adminPassword));
                admin.setRole(Role.ADMIN);
                admin.setActive(true);

                userRepository.save(admin);

                System.out.println("✅ Admin initialized from config");
            }
        };
    }
}