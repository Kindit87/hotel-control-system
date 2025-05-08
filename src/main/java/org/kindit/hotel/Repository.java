package org.kindit.hotel;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.kindit.hotel.user.UserRepository;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Getter
public class Repository {

    private final UserRepository userRepository;
}
