package io.teletronics.storage_app.service.impl;

import com.mongodb.DuplicateKeyException;
import io.teletronics.storage_app.document.UserDocument;
import io.teletronics.storage_app.dto.request.AuthRequest;
import io.teletronics.storage_app.dto.response.TokenResponse;
import io.teletronics.storage_app.exception.InvalidCredentialsException;
import io.teletronics.storage_app.exception.UserExistException;
import io.teletronics.storage_app.exception.UserNotFoundException;
import io.teletronics.storage_app.repository.UserRepository;
import io.teletronics.storage_app.security.JwtUtil;
import io.teletronics.storage_app.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthenticationServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder,
                                     JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> register(AuthRequest authRequest) {
        return userRepository.findByUsername(authRequest.getUsername())
                .flatMap(existingUser -> Mono.error(UserExistException::new))
                .switchIfEmpty(Mono.defer(() -> {
                    UserDocument user = new UserDocument();
                    user.setUsername(authRequest.getUsername());
                    user.setPassword(passwordEncoder.encode(authRequest.getPassword()));
                    return userRepository.save(user);
                }))
                .then()
                .onErrorMap(DuplicateKeyException.class, e -> new UserExistException());
    }

    @Override
    public Mono<Void> refreshToken(String token) {
        return null;
    }

    @Override
    public Mono<TokenResponse> login(AuthRequest authRequest) {
        return userRepository.findByUsername(authRequest.getUsername())
                .switchIfEmpty(Mono.error(UserNotFoundException::new))
                .filter(user -> passwordEncoder.matches(authRequest.getPassword(), user.getPassword()))
                .switchIfEmpty(Mono.error(InvalidCredentialsException::new))
                .flatMap(user -> {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword());
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    String token = jwtUtil.generateToken(user.getUsername());
                    return Mono.just(new TokenResponse(token));
                });
    }
}
