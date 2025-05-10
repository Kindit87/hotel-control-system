package org.kindit.hotel.endpoits.user.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.kindit.hotel.data.user.Role;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatchUserRequest {
    private String firstname;
    private String lastname;
    private String email;
    private String password;
    private Role role;
}
