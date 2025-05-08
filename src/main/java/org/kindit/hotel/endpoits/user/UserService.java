package org.kindit.hotel.endpoits.user;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.kindit.hotel.ControllerService;
import org.kindit.hotel.endpoits.user.request.UserRequest;
import org.kindit.hotel.user.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService extends ControllerService {

    private final PasswordEncoder passwordEncoder;

    public List<User> getAll() {
        return repository.getUserRepository().findAll();
    }

    public Optional<User> get(Integer id) {
        return repository.getUserRepository().findById(id);
    }

    public User post(UserRequest request) {
        repository.getUserRepository().findByEmail(request.getEmail()).ifPresent(u -> {
            throw new IllegalArgumentException("User with this email already exists");
        });

        User user = User.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();

        repository.getUserRepository().save(user);

        return user;
    }

    public User put(Integer id, UserRequest request) {
        User existingUser = repository.getUserRepository().findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User with id " + id + " not found"));

        repository.getUserRepository().findByEmail(request.getEmail()).ifPresent(user -> {
            if (!Objects.equals(user.getId(), existingUser.getId()))
                throw new IllegalArgumentException("Email is already taken by another user");
        });

        existingUser.setFirstname(request.getFirstname());
        existingUser.setLastname(request.getLastname());
        existingUser.setEmail(request.getEmail());
        existingUser.setRole(request.getRole());
        existingUser.setPassword(passwordEncoder.encode(request.getPassword()));

        return repository.getUserRepository().save(existingUser);
    }
}
