package com.mdswaley.BookingApplication.Config;

import com.mdswaley.BookingApplication.Entity.User;
import com.mdswaley.BookingApplication.Enums.Role;
import com.mdswaley.BookingApplication.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        createAdmin();
        createUser();
    }

    private void createAdmin() {

        if (!userRepository.existsByUsername("admin")) {

            User admin = User.builder()
                    .username("admin")
                    .email("admin@example.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .build();

            userRepository.save(admin);

            System.out.println("ADMIN user created");
        }
    }

    private void createUser() {

        if (!userRepository.existsByUsername("user")) {

            User user = User.builder()
                    .username("user")
                    .email("user@example.com")
                    .password(passwordEncoder.encode("user123"))
                    .role(Role.USER)
                    .build();

            userRepository.save(user);

            System.out.println("USER user created");
        }
    }
}
