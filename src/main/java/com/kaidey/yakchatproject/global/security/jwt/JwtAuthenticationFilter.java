package com.kaidey.yakchatproject.global.security.jwt;

import com.kaidey.yakchatproject.domain.user.service.UserDetailsServiceImpl;
import com.kaidey.yakchatproject.global.util.RedisUtil;
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

    @Autowired
    private RedisUtil redisUtil;

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

                // ★ 블랙리스트 토큰 체크 추가
                if (isTokenBlacklisted(token)) {
                    writeUnauthorized(response, "TOKEN_BLACKLISTED", "토큰이 무효화되었습니다.");
                    return;
                }

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
            writeUnauthorized(response, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다.");
        } catch (IllegalArgumentException e) { // userId 누락/형식 오류 등
            writeUnauthorized(response, "INVALID_TOKEN", "유효하지 않은 토큰입니다.");
        } catch (Exception e) {
            writeUnauthorized(response, "AUTH_ERROR", "인증에 실패했습니다.");
            logger.error("Authentication failed", e); // 디버깅용 로그
        }
    }

    /**
     * 토큰이 블랙리스트에 있는지 확인
     */
    private boolean isTokenBlacklisted(String token) {
        try {
            String blacklistKey = "blacklist:token:" + token;
            String result = redisUtil.getData(blacklistKey);
            return result != null; // 데이터가 있으면 블랙리스트에 있음
        } catch (Exception e) {
            logger.warn("블랙리스트 확인 중 오류: " + e.getMessage());
            return false; // 오류 시 false 반환하여 서비스 중단 방지
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