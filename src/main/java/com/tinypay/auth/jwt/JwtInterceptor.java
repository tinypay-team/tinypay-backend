package com.tinypay.auth.jwt;

import com.tinypay.global.exception.CustomException;
import com.tinypay.global.exception.ErrorType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String accessToken = resolveAccessToken(request);

        if (accessToken == null || !jwtTokenProvider.validateAccessToken(accessToken)) {
            throw new CustomException(ErrorType.INVALID_ID_TOKEN);
        }

        Long userId = jwtTokenProvider.extractUserId(accessToken);
        request.setAttribute("userId", userId);
        return true;
    }

    private String resolveAccessToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}