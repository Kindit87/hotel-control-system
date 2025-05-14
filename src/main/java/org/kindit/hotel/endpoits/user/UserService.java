package org.kindit.hotel.endpoits.user;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.kindit.hotel.endpoits.ServiceController;
import org.kindit.hotel.endpoits.user.request.UserRequest;
import org.kindit.hotel.data.user.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService extends ServiceController {

    private final PasswordEncoder passwordEncoder;
    private final String uploadDir = "uploads/rooms/";

    public List<User> getAll() {
        return repository.getUserRepository().findAll();
    }

    public Optional<User> get(Integer id) {
        return repository.getUserRepository().findById(id);
    }

    public Optional<User> getMe() {
        return Optional.of(getAuthentifactedUser());
    }

    public User post(UserRequest request) {
        String imageName = "";

        repository.getUserRepository().findByEmail(request.getEmail()).ifPresent(u -> {
            throw new IllegalArgumentException("User with this email already exists");
        });

        if (request.getImage() != null && !request.getImage().isEmpty()) {
            imageName = saveImage(Path.of(uploadDir), request.getImage());
        }

        User user = User.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .image(imageName)
                .build();

        repository.getUserRepository().save(user);

        return user;
    }

    public User put(Integer id, UserRequest request) {
        String imageName = "";

        User existingUser = repository.getUserRepository().findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User with id " + id + " not found"));

        repository.getUserRepository().findByEmail(request.getEmail()).ifPresent(user -> {
            if (!Objects.equals(user.getId(), existingUser.getId()))
                throw new IllegalArgumentException("Email is already taken by another user");
        });

        if (request.getImage() != null && !request.getImage().isEmpty()) {
            imageName = saveImage(Path.of(uploadDir), request.getImage());
        }

        existingUser.setFirstname(request.getFirstname());
        existingUser.setLastname(request.getLastname());
        existingUser.setEmail(request.getEmail());
        existingUser.setRole(request.getRole());
        existingUser.setPassword(passwordEncoder.encode(request.getPassword()));
        existingUser.setImage(imageName);

        return repository.getUserRepository().save(existingUser);
    }

    public User patch(Integer id, UserRequest request) {
        User existingUser = repository.getUserRepository().findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User with id " + id + " not found"));

        if (request.getFirstname() != null) {
            existingUser.setFirstname(request.getFirstname());
        }
        if (request.getLastname() != null) {
            existingUser.setLastname(request.getLastname());
        }
        if (request.getEmail() != null) {
            // Можно проверить на уникальность email тут тоже, если хочешь
            existingUser.setEmail(request.getEmail());
        }

        if (request.getImage() != null && !request.getImage().isEmpty()) {
            existingUser.setImage(saveImage(Path.of(uploadDir), request.getImage()));
        }

        if (request.getPassword() != null) {
            existingUser.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getRole() != null) {
            existingUser.setRole(request.getRole());
        }

        return repository.getUserRepository().save(existingUser);
    }

    public boolean delete(Integer id) {
        return repository.getUserRepository()
                .findById(id)
                .map(user -> {
                    repository.getUserRepository().delete(user);
                    return true;
                })
                .orElse(false);
    }
}
