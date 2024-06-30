package io.teletronics.storage_app.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import reactor.core.publisher.Mono;

import java.util.Objects;


@Configuration
@EnableWebFluxSecurity
public class SecurityConfiguration {

    private static final String TOKEN_HEADER_BEARER = "Bearer ";
    private static final int TOKEN_HEADER_BEARER_LENGTH = 7;

    private final JwtUtil jwtUtil;
    private final ReactiveUserDetailsService reactiveUserDetailsService;

    @Autowired
    public SecurityConfiguration(JwtUtil jwtUtil, ReactiveUserDetailsService reactiveUserDetailsService) {
        this.jwtUtil = jwtUtil;
        this.reactiveUserDetailsService = reactiveUserDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers(
                "/swagger-ui/**", "/v3/api-docs/**"
        );
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/api/authenticate/**", "/api/files/download/by-link/**",
                                "/swagger-ui.html", "/v3/api-docs/**", "/swagger-ui/**").permitAll()
                        .anyExchange().authenticated()
                )
                .addFilterAt(jwtAuthenticationWebFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
                .build();

    }

    @Bean
    public ReactiveAuthenticationManager reactiveAuthenticationManager() {
        return authentication -> {
            String token = authentication.getCredentials().toString();
            String username = jwtUtil.extractUsername(token);
            return reactiveUserDetailsService.findByUsername(username)
                    .filter(userDetails -> jwtUtil.validateToken(token, userDetails.getUsername()))
                    .map(userDetails -> new UsernamePasswordAuthenticationToken(
                            userDetails, token, userDetails.getAuthorities()));
        };
    }

    private AuthenticationWebFilter jwtAuthenticationWebFilter() {
        AuthenticationWebFilter authenticationWebFilter = new AuthenticationWebFilter(reactiveAuthenticationManager());
        authenticationWebFilter.setServerAuthenticationConverter(jwtAuthenticationConverter());
        authenticationWebFilter.setRequiresAuthenticationMatcher(ServerWebExchangeMatchers.pathMatchers("/api/**"));
        return authenticationWebFilter;
    }

    private ServerAuthenticationConverter jwtAuthenticationConverter() {
        return exchange -> {
            String token = extractTokenFromRequest(exchange.getRequest());
            return Objects.isNull(token)
                    ? Mono.empty()
                    : Mono.just(new UsernamePasswordAuthenticationToken(token, token));
        };
    }

    private String extractTokenFromRequest(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith(TOKEN_HEADER_BEARER)) {
            return authHeader.substring(TOKEN_HEADER_BEARER_LENGTH);
        }
        return null;
    }
}