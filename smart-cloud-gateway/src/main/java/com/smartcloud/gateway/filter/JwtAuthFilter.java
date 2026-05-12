package com.smartcloud.gateway.filter;

import com.smartcloud.common.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private final Environment env;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public JwtAuthFilter(Environment env) {
        this.env = env;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (isWhiteListed(path)) {
            return chain.filter(exchange);
        }

        String token = extractToken(exchange.getRequest());
        if (token == null || !JwtUtils.validate(token)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        Long userId = JwtUtils.getUserId(token);
        String username = JwtUtils.getUsername(token);

        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header("X-User-Id", String.valueOf(userId))
                .header("X-Username", username)
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    private String extractToken(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private boolean isWhiteListed(String path) {
        String whiteList = env.getProperty("gateway.white-list", "");
        List<String> paths = Arrays.asList(whiteList.split(","));
        return paths.stream().anyMatch(p -> pathMatcher.match(p.trim(), path));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
