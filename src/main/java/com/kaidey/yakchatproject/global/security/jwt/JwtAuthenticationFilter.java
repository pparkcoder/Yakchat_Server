package com.kaidey.yakchatproject.global.security.jwt;

import com.kaidey.yakchatproject.domain.user.service.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String bearer = request.getHeader("Authorization");
        String token = (bearer != null && bearer.startsWith("Bearer "))
                ? bearer.substring(7)
                : null;

        try {
            // 이미 인증되어 있으면 패스
            if (token != null
                    && SecurityContextHolder.getContext().getAuthentication() == null
                    && jwtTokenProvider.validateToken(token)) {

                // ★ username(닉네임/이메일) 대신 토큰의 userId로 식별
                Long userId = jwtTokenProvider.getUserIdFromToken(token);
                if (userId == null) {
                    throw new IllegalArgumentException("userId missing in token");
                }

                UserDetails userDetails = userDetailsService.loadUserById(userId);

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(auth);
            }

            filterChain.doFilter(request, response);

        } catch (UsernameNotFoundException e) {
            writeUnauthorized(response, "USER_NOT_FOUND", "User not found.");
        } catch (IllegalArgumentException e) { // userId 누락/형식 오류 등
            writeUnauthorized(response, "INVALID_TOKEN", "Invalid authentication token.");
        } catch (Exception e) {
            writeUnauthorized(response, "AUTH_ERROR", "Authentication failed.");
            e.printStackTrace(); // 디버깅용 로그
        }
    }

    private void writeUnauthorized(HttpServletResponse response, String code, String message) throws IOException {
        if (!response.isCommitted()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":\"" + code + "\",\"message\":\"" + message + "\"}");
        }
    }
}