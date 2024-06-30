package io.teletronics.storage_app.util;

import io.teletronics.storage_app.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class Helper {
    private final JwtUtil jwtUtil;

    @Autowired
    public Helper(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    public Mono<String> getUsernameFromTokenAsMono(ServerWebExchange exchange) {
        return Mono.just(jwtUtil.getUsernameFromToken(exchange));
    }

    public String getUsernameFromToken(ServerWebExchange exchange) {
        return jwtUtil.getUsernameFromToken(exchange);
    }

}
