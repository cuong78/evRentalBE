package com.group4.evRentalBE.config;


import com.group4.evRentalBE.exception.exceptions.UnauthorizedException;
import com.group4.evRentalBE.model.entity.User;
import com.group4.evRentalBE.service.TokenService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
import java.util.List;

@Component
public class Filter extends OncePerRequestFilter {

    private final HandlerExceptionResolver resolver;
    private final TokenService tokenService;

    @Autowired
    public Filter(@Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver, TokenService tokenService) {
        this.resolver = resolver;
        this.tokenService = tokenService;
    }

    // List.of mình đang gán cứng dữ liệu ,  ko cho phép thêm xóa sửa khi gọi biến public_api
    // list.of ko được null
    List<String> publicAPI = List.of(
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/api/login",
            "/api/register",
            "/api/refresh-token",
            "/api/google-login",
            "/api/payments/vnpay-return",
            "/api/facebook-login",
            "/api/reset-password",
            "/api/forgot-password",
            "/api/verify",
            "/api/vehicles/search"

            );

    // kiểm tra xem request có thuộc danh sách publicAPI
    boolean isPermitted(HttpServletRequest request) {
        AntPathMatcher patchMatch = new AntPathMatcher();
        String uri = request.getRequestURI();
        String method = request.getMethod();
        // kiểm tra thử uri và method của request có bằng với dữ liệu mình cấu hin
        if (method.equals("GET") && patchMatch.match("/api/product/**", uri)) {
            return true; // public api
        }
        // chuyển đổi list thành một stream
        return publicAPI.stream()
                // trong stream có thư viện anyMatch
                // anyMatch duyệt qua từng phần tử
                // item có nghĩa đang đại diện cho list publis_api
                // so sánh uri  với item
                .anyMatch(item -> patchMatch.match(item, uri));
    }

    @Override
    protected void doFilterInternal(
            @Nonnull HttpServletRequest request,
            @Nonnull HttpServletResponse response,
            @Nonnull FilterChain filterChain)
            throws ServletException, IOException {

        if (isPermitted(request)) {
            // public API
            filterChain.doFilter(request, response);
        } else {
            // không phải là public API => check role
            String token = getToken(request);

            if (token == null) {
                // chưa đăng nhập => quăng lỗi
                resolver.resolveException(
                        request, response, null, new UnauthorizedException("Authentication token is missing!"));
                return;
            }

            User user;
            try {
                user = tokenService.getAccountByToken(token);
            } catch (ExpiredJwtException e) {
                resolver.resolveException(
                        request, response, null, new UnauthorizedException("Authentication token is expired!"));
                return;
            } catch (JwtException | IllegalArgumentException e) {
                resolver.resolveException(
                        request, response, null, new UnauthorizedException("Authentication token is invalid!"));
                return;
            }

            // => token chuẩn
            if (user == null) {
                resolver.resolveException(
                        request, response, null, new UnauthorizedException("User not found for the provided token!"));
                return;
            }

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(user, token, user.getAuthorities());
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
            filterChain.doFilter(request, response);
        }
    }

    String getToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null) return null;
        return token.substring(7);
    }
}
