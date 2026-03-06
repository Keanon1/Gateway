package com.example.demo.Filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;


@Component
public class CookieToAuthFilter extends AbstractGatewayFilterFactory<Object> {

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {

            var cookies = exchange.getRequest().getCookies();

            if (cookies.containsKey("JWT_TOKEN")) {
                String jwt = cookies.getFirst("JWT_TOKEN").getValue();

                var mutated = exchange.getRequest()
                        .mutate()
                        .header("Authorization", "Bearer " + jwt)
                        .build();

                return chain.filter(exchange.mutate().request(mutated).build());
            }

            return chain.filter(exchange);
        };
    }
}

