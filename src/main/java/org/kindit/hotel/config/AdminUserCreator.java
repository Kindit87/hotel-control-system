package org.kindit.hotel.config;

import lombok.RequiredArgsConstructor;
import org.kindit.hotel.data.user.Role;
import org.kindit.hotel.data.user.User;
import org.kindit.hotel.data.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminUserCreator implements CommandLineRunner {

    private final UserRepository userRepository;

    @Override
    public void run(String... args) {
        User admin = User.builder()
                .email("admin")
                .role(Role.ADMIN)
                .password("AdminSuperSecretPassword")
                .build();

        userRepository.save(admin);
    }
}
