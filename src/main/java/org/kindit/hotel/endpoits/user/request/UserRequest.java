package org.kindit.hotel.endpoits.user.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.kindit.hotel.data.user.Role;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequest {
    private String firstname;
    private String lastname;
    private String email;
    private String password;
    private Role role;
    private MultipartFile image;
}
