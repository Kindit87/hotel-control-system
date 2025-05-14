package org.kindit.hotel.endpoits.user.request;

import lombok.Data;
import org.kindit.hotel.data.user.Role;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UserRequest {
    private String firstname;
    private String lastname;
    private String email;
    private String password;
    private Role role;
    private MultipartFile image;
}
