package com.group4.evRentalBE.infrastructure.security;

import com.group4.evRentalBE.domain.entity.User;
import com.group4.evRentalBE.infrastructure.exception.exceptions.UnauthorizedException;
import com.group4.evRentalBE.business.service.TokenService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;


@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final HandlerExceptionResolver exceptionResolver;
    private final TokenService tokenService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Autowired
    public JwtAuthenticationFilter(
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver,
            TokenService tokenService) {
        this.exceptionResolver = exceptionResolver;
        this.tokenService = tokenService;
    }

    /**
     * Kiểm tra request có phải public API không
     */
    private boolean isPublicEndpoint(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String method = request.getMethod();

        // Cho phép GET request tới /api/product/**
        if ("GET".equals(method) && pathMatcher.match("/api/product/**", uri)) {
            return true;
        }

        // Kiểm tra với danh sách public endpoints
        return SecurityConstants.PUBLIC_ENDPOINTS.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, uri));
    }

    /**
     * Extract token từ request header
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(SecurityConstants.AUTHORIZATION_HEADER);
        if (bearerToken != null && bearerToken.startsWith(SecurityConstants.BEARER_PREFIX)) {
            return bearerToken.substring(SecurityConstants.BEARER_PREFIX_LENGTH);
        }
        return null;
    }

    @Override
    protected void doFilterInternal(
            @Nonnull HttpServletRequest request,
            @Nonnull HttpServletResponse response,
            @Nonnull FilterChain filterChain)
            throws ServletException, IOException {

        // Nếu là public API, cho phép truy cập
        if (isPublicEndpoint(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Lấy token từ header
        String token = extractTokenFromRequest(request);

        if (token == null) {
            exceptionResolver.resolveException(
                    request, response, null,
                    new UnauthorizedException("Authentication token is missing!"));
            return;
        }

        // Xác thực token
        try {
            User user = tokenService.validateAndGetUser(token);

            if (user == null) {
                exceptionResolver.resolveException(
                        request, response, null,
                        new UnauthorizedException("User not found for the provided token!"));
                return;
            }

            // Set authentication vào SecurityContext
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            user, token, user.getAuthorities());
            authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            log.warn("Expired JWT token for request: {}", request.getRequestURI());
            exceptionResolver.resolveException(
                    request, response, null,
                    new UnauthorizedException("Authentication token is expired!"));
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT token for request: {}", request.getRequestURI());
            exceptionResolver.resolveException(
                    request, response, null,
                    new UnauthorizedException("Authentication token is invalid!"));
        }
    }
}