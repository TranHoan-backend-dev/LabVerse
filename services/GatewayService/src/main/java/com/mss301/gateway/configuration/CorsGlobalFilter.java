package com.mss301.gateway.configuration;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class CorsGlobalFilter implements GlobalFilter, Ordered {

    public CorsGlobalFilter() {
        System.out.println("========================================");
        System.out.println("CorsGlobalFilter initialized!");
        System.out.println("========================================");
    }

    // Allow localhost and common local IP patterns
    private static final String[] ALLOWED_ORIGIN_PATTERNS = {
        "http://localhost:3000",
        "http://127.0.0.1:3000",
        "http://192.168.*.*:3000",  // Allow any IP in 192.168.x.x range
        "http://10.*.*.*:3000",     // Allow any IP in 10.x.x.x range
        "http://172.16.*.*:3000",   // Allow IPs in 172.16-31.x.x range
        "http://172.17.*.*:3000",
        "http://172.18.*.*:3000",
        "http://172.19.*.*:3000",
        "http://172.20.*.*:3000",
        "http://172.21.*.*:3000",
        "http://172.22.*.*:3000",
        "http://172.23.*.*:3000",
        "http://172.24.*.*:3000",
        "http://172.25.*.*:3000",
        "http://172.26.*.*:3000",
        "http://172.27.*.*:3000",
        "http://172.28.*.*:3000",
        "http://172.29.*.*:3000",
        "http://172.30.*.*:3000",
        "http://172.31.*.*:3000"
    };
    private static final String ALLOWED_METHODS = "GET,POST,PUT,PATCH,DELETE,OPTIONS,HEAD";
    private static final String ALLOWED_HEADERS = "*";
    private static final String EXPOSED_HEADERS = "*";
    private static final boolean ALLOW_CREDENTIALS = true;
    private static final long MAX_AGE = 3600L;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        HttpHeaders headers = response.getHeaders();

        String origin = request.getHeaders().getFirst(HttpHeaders.ORIGIN);
        String method = request.getMethod() != null ? request.getMethod().name() : "UNKNOWN";
        String path = request.getURI().getPath();
        
        // Always log to see if filter is triggered
        System.out.println("========================================");
        System.out.println("CORS Filter triggered!");
        System.out.println("Method: " + method);
        System.out.println("Origin: " + origin);
        System.out.println("Path: " + path);
        System.out.println("========================================");
        
        // Handle preflight request (OPTIONS) - MUST handle BEFORE forwarding to downstream
        if (request.getMethod() == HttpMethod.OPTIONS) {
            System.out.println("CORS: Handling OPTIONS preflight request");
            boolean isAllowed = origin != null && isAllowedOrigin(origin);
            System.out.println("CORS: Origin allowed: " + isAllowed + " for origin: " + origin);
            
            if (isAllowed) {
                // Set CORS headers BEFORE completing response
                headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
                headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, ALLOWED_METHODS);
                headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, ALLOWED_HEADERS);
                headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, String.valueOf(ALLOW_CREDENTIALS));
                headers.set(HttpHeaders.ACCESS_CONTROL_MAX_AGE, String.valueOf(MAX_AGE));
                
                // Set status BEFORE writing response
                response.setStatusCode(HttpStatus.OK);
                
                System.out.println("CORS: OPTIONS preflight handled, headers: " + headers);
                
                // Write empty body to ensure headers are flushed
                // This is critical - headers must be written before response completes
                return response.writeWith(Mono.just(response.bufferFactory().wrap(new byte[0])))
                    .then(response.setComplete());
            } else {
                System.out.println("CORS: Origin NOT allowed: " + origin);
                response.setStatusCode(HttpStatus.FORBIDDEN);
                return response.setComplete();
            }
        }

        // For non-OPTIONS requests, set CORS headers BEFORE forwarding to downstream
        // This ensures headers are present even if downstream service fails
        if (origin != null && isAllowedOrigin(origin)) {
            // Set headers immediately - they will be preserved through the filter chain
            headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
            headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, String.valueOf(ALLOW_CREDENTIALS));
            headers.set(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, EXPOSED_HEADERS);
            System.out.println("CORS: Headers set for " + method + " request BEFORE filter chain");
        } else {
            System.out.println("CORS: Origin not allowed or null: " + origin);
        }

        // Continue filter chain - headers already set above will be preserved
        // Note: Don't try to set headers in callbacks as response may be committed (read-only)
        return chain.filter(exchange);
    }

    private boolean isAllowedOrigin(String origin) {
        if (origin == null || origin.isEmpty()) {
            System.out.println("CORS: Origin is null or empty");
            return false;
        }
        
        System.out.println("CORS: Checking origin: " + origin);
        
        // Check exact matches first
        for (String pattern : ALLOWED_ORIGIN_PATTERNS) {
            if (pattern.equals(origin)) {
                System.out.println("CORS: Exact match found: " + pattern);
                return true;
            }
        }
        
        // Allow any localhost or local IP on port 3000 for development
        // This regex matches:
        // - localhost:3000
        // - 127.0.0.1:3000
        // - 192.168.x.x:3000 (e.g., 192.168.1.18:3000)
        // - 10.x.x.x:3000
        // - 172.16-31.x.x:3000
        String localIpRegex = "http://(localhost|127\\.0\\.0\\.1|192\\.168\\.\\d+\\.\\d+|10\\.\\d+\\.\\d+\\.\\d+|172\\.(1[6-9]|2[0-9]|3[0-1])\\.\\d+\\.\\d+):3000";
        boolean matches = origin.matches(localIpRegex);
        System.out.println("CORS: Regex match (" + localIpRegex + "): " + matches);
        if (matches) {
            return true;
        }
        
        System.out.println("CORS: Origin NOT allowed: " + origin);
        return false;
    }

    @Override
    public int getOrder() {
        // Lower number = higher priority
        // Use a very high priority to run BEFORE route matching and forwarding
        // This ensures OPTIONS requests are handled at Gateway level, not forwarded to downstream
        return -2147483647; // Integer.MIN_VALUE + 1 (just after RemoveCachedBodyFilter)
    }
}

