package org.kindit.hotel.endpoits.auth;

import lombok.RequiredArgsConstructor;
import org.kindit.hotel.endpoits.ApiController;
import org.kindit.hotel.endpoits.auth.request.AuthenticationRequest;
import org.kindit.hotel.endpoits.auth.request.RegisterRequest;
import org.kindit.hotel.endpoits.auth.response.AuthenticationResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController extends ApiController<AuthenticationService> {

    private final AuthenticationService service;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(service.register(request));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest request
    ) {
        return ResponseEntity.ok(service.authenticate(request));
    }
}
