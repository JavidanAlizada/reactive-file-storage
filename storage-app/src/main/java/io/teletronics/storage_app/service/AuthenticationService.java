package io.teletronics.storage_app.service;

import io.teletronics.storage_app.dto.request.AuthRequest;
import io.teletronics.storage_app.dto.response.TokenResponse;
import reactor.core.publisher.Mono;

public interface AuthenticationService {

    Mono<Void> register(AuthRequest authRequest);

    Mono<Void> refreshToken(String token);

    Mono<TokenResponse> login(AuthRequest authRequest);
}
