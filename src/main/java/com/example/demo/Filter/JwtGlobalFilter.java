package com.example.demo.Filter;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.Key;

@Component
public class JwtGlobalFilter implements GlobalFilter {

    private static final String SECRET =
            "test-secret-key-123456-test-secret-key-123456";

    private static final Key key =
            Keys.hmacShaKeyFor(SECRET.getBytes());

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();

        // Allow auth routes
        if (path.contains("/auth") || path.contains("/test-login") || path.equals("/")) {
            return chain.filter(exchange);
        }

        String token = null;

        // Extract JWT from cookie
        if(exchange.getRequest().getCookies().containsKey("jwt")){
            token = exchange.getRequest()
                    .getCookies()
                    .getFirst("jwt")
                    .getValue();
        }

        if(token == null){
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        try {
            // Validate JWT signature
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);

            // Mutate request to add Authorization header for downstream services
            ServerHttpRequest mutatedRequest = exchange.getRequest()
                    .mutate()
                    .header("Authorization", "Bearer " + token)
                    .build();

            // Pass mutated exchange downstream
            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch(Exception e) {
        // THIS LINE IS THE MOST IMPORTANT:
        System.err.println("!!! GATEWAY ERROR: " + e.getMessage());
        e.printStackTrace();

        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

}
}