package com.dev24.bookstore.auth.controller;

import java.time.Duration;
import java.time.Instant;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dev24.bookstore.auth.domain.Admin;
import com.dev24.bookstore.auth.domain.Customer;
import com.dev24.bookstore.auth.dto.CustomerSignUpRequest;
import com.dev24.bookstore.auth.dto.CustomerSignUpResponse;
import com.dev24.bookstore.auth.dto.LoginRequest;
import com.dev24.bookstore.auth.dto.RefreshTokenRequest;
import com.dev24.bookstore.auth.dto.TokenResponse;
import com.dev24.bookstore.auth.security.AccessTokenBlacklist;
import com.dev24.bookstore.auth.security.AccessTokenClaims;
import com.dev24.bookstore.auth.security.BearerTokenResolver;
import com.dev24.bookstore.auth.security.JwtTokenProvider;
import com.dev24.bookstore.auth.security.RefreshTokenPayload;
import com.dev24.bookstore.auth.security.RefreshTokenStore;
import com.dev24.bookstore.auth.service.AdminService;
import com.dev24.bookstore.auth.service.CustomerService;
import com.dev24.bookstore.auth.service.CustomerSignUpCommand;
import com.dev24.bookstore.common.exception.BusinessException;
import com.dev24.bookstore.common.exception.ErrorCode;
import com.dev24.bookstore.common.response.ApiResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final CustomerService customerService;
    private final AdminService adminService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenStore refreshTokenStore;
    private final AccessTokenBlacklist accessTokenBlacklist;

    // 고객 회원가입 - 비밀번호를 BCrypt로 해싱해 저장
    @PostMapping("/customers/signup")
    public ApiResponse<CustomerSignUpResponse> signUpCustomer(@Valid @RequestBody CustomerSignUpRequest request) {
        CustomerSignUpCommand command = new CustomerSignUpCommand(
                request.loginId(), request.password(), request.name(), request.nickname(),
                request.email(), request.phone(), request.address(), request.interest(), request.newsletterYn());
        Customer customer = customerService.signUp(command);
        return ApiResponse.success(new CustomerSignUpResponse(customer.getId(), customer.getLoginId(), customer.getName()));
    }

    // 고객 로그인 - 인증 성공 시 액세스 토큰 발급 + 리프레시 토큰 Redis에 저장
    @PostMapping("/customers/login")
    public ApiResponse<TokenResponse> loginCustomer(@Valid @RequestBody LoginRequest request) {
        Customer customer = customerService.authenticate(request.loginId(), request.password());
        String accessToken = jwtTokenProvider.generateAccessToken(customer.getLoginId(), customer.getRole());
        String refreshToken = refreshTokenStore.issue(customer.getLoginId(), customer.getRole());
        return ApiResponse.success(TokenResponse.of(accessToken, refreshToken));
    }

    // 관리자 로그인 - 자기가입 없이 기존 관리자 계정으로만 인증(고객 로그인과 동일한 토큰 발급 흐름)
    @PostMapping("/admins/login")
    public ApiResponse<TokenResponse> loginAdmin(@Valid @RequestBody LoginRequest request) {
        Admin admin = adminService.authenticate(request.loginId(), request.password());
        String accessToken = jwtTokenProvider.generateAccessToken(admin.getLoginId(), admin.getRole());
        String refreshToken = refreshTokenStore.issue(admin.getLoginId(), admin.getRole());
        return ApiResponse.success(TokenResponse.of(accessToken, refreshToken));
    }

    // 액세스 토큰 재발급 - Redis에 저장된 리프레시 토큰으로 신원 확인 후 새 액세스 토큰만 발급(리프레시 토큰은 그대로 재사용)
    @PostMapping("/refresh")
    public ApiResponse<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        RefreshTokenPayload payload = refreshTokenStore.find(request.refreshToken())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN));
        String accessToken = jwtTokenProvider.generateAccessToken(payload.loginId(), payload.role());
        return ApiResponse.success(TokenResponse.of(accessToken, request.refreshToken()));
    }

    // 로그아웃 - 현재 액세스 토큰을 남은 유효시간만큼 블랙리스트에 등록하고, 리프레시 토큰은 Redis에서 즉시 삭제
    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest servletRequest, @Valid @RequestBody RefreshTokenRequest request) {
        String accessToken = BearerTokenResolver.resolve(servletRequest);
        // /logout은 인증이 필요한 경로라 필터를 통과했다면 파싱은 항상 성공한다(방어적으로 예외 처리)
        AccessTokenClaims claims = jwtTokenProvider.parse(accessToken)
                .orElseThrow(() -> new IllegalStateException("인증된 요청인데 액세스 토큰 파싱에 실패했습니다"));
        Duration remainingTtl = Duration.between(Instant.now(), claims.expiration().toInstant());
        accessTokenBlacklist.blacklist(claims.jti(), remainingTtl);
        refreshTokenStore.revoke(request.refreshToken());
        return ApiResponse.success();
    }
}
