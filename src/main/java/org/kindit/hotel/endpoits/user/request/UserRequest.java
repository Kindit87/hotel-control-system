package org.kindit.hotel.endpoits.user.request;

import lombok.Data;
import org.kindit.hotel.user.Role;

@Data
public class UserRequest {
    private String firstname;
    private String lastname;
    private String email;
    private String password;
    private Role role;
}
