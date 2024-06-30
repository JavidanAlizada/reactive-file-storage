package io.teletronics.storage_app.controller;

import io.teletronics.storage_app.dto.request.AuthRequest;
import io.teletronics.storage_app.dto.response.TokenResponse;
import io.teletronics.storage_app.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/authenticate")
public class AuthController {

    private static final String TOKEN_HEADER_BEARER = "Bearer ";
    private static final int TOKEN_HEADER_BEARER_LENGTH = 7;

    private final AuthenticationService authenticationService;

    @Autowired
    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public Mono<TokenResponse> login(@RequestBody @Validated AuthRequest authRequest) {
        return this.authenticationService.login(authRequest);
    }

    @PostMapping("/register")
    public Mono<ResponseEntity<Void>> register(@RequestBody @Validated AuthRequest authRequest,
                                               UriComponentsBuilder uriBuilder) {
        return this.authenticationService.register(authRequest)
                .then(Mono.just(ResponseEntity.status(HttpStatus.CREATED).build()));

    }

    @PostMapping("/refresh-token")
    public Mono<Void> invalidateTokens(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        return this.authenticationService.refreshToken(extractTokenFromRequest(authHeader));
    }

    protected String extractTokenFromRequest(String authHeader) {
        if (authHeader != null && authHeader.startsWith(TOKEN_HEADER_BEARER)) {
            return authHeader.substring(TOKEN_HEADER_BEARER_LENGTH);
        }
        return null;
    }

}
