package org.kindit.hotel.endpoits.user;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.kindit.hotel.ApiController;
import org.kindit.hotel.Repository;
import org.kindit.hotel.endpoits.user.request.UserRequest;
import org.kindit.hotel.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController extends ApiController<UserService> {

    private final Repository repository;

    @GetMapping("/all")
    public ResponseEntity<List<User>> getAll() {
        List<User> users = service.getAll();

        if (users.isEmpty())
            return ResponseEntity.noContent().build();

        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> get(@PathVariable Integer id) {
        if (id < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Incorrect id");
        }

        return service.get(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}")
    public ResponseEntity<User> post(@PathVariable Integer id, @Validated @RequestBody UserRequest request) {
        if (repository.getUserRepository().findByEmail(request.getEmail()).isEmpty())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User with this email already exists");

        User user = service.post(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> put(@PathVariable Integer id, @Validated @RequestBody UserRequest request) {
        try {
            User updatedUser = service.put(id, request);
            return ResponseEntity.ok(updatedUser);

        } catch (EntityNotFoundException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(null);

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(null);
        }
    }


}
