package org.kindit.hotel.config;

import lombok.RequiredArgsConstructor;
import org.kindit.hotel.data.user.Role;
import org.kindit.hotel.data.user.User;
import org.kindit.hotel.data.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminUserCreator implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        User admin = User.builder()
                .email("admin@example.com")
                .role(Role.ADMIN)
                .password(passwordEncoder.encode("AdminSuperSecretPassword"))
                .build();

        userRepository.save(admin);
    }
}
