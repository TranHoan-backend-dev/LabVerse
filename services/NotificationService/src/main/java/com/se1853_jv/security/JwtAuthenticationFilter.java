package com.se1853_jv.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String subject; // Đây là userId UUID

        // 1. Kiểm tra xem có header 'Authorization' và có 'Bearer ' không
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Lấy token từ header
        jwt = authHeader.substring(7);

        try {
            // 3. Giải mã token để lấy subject (userId)
            subject = jwtService.extractSubject(jwt);

            // 4. Nếu lấy được subject VÀ user chưa được xác thực trong context
            if (subject != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // 5. Kiểm tra token có hợp lệ không
                if (jwtService.isTokenValid(jwt)) {
                    // 6. Tạo đối tượng Authentication
                    // Service này không cần UserDetails, chỉ cần userId là đủ
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            subject, // principal chính là userId (dạng String)
                            null,
                            null // không có quyền (authorities)
                    );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    // 7. "Nhét" thông tin user vào SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Token không hợp lệ (hết hạn, sai chữ ký, v.v.)
            // Không làm gì, để request đi tiếp và bị chặn ở SecurityConfig
            logger.error("Cannot set user authentication: " + e.getMessage(), e);

        }

        // 8. Chuyển request cho filter tiếp theo
        filterChain.doFilter(request, response);
    }
}