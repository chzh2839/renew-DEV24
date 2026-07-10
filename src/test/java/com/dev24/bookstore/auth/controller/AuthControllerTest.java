package com.dev24.bookstore.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Date;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.dev24.bookstore.auth.domain.Admin;
import com.dev24.bookstore.auth.domain.Customer;
import com.dev24.bookstore.auth.domain.Role;
import com.dev24.bookstore.auth.security.AccessTokenBlacklist;
import com.dev24.bookstore.auth.security.AccessTokenClaims;
import com.dev24.bookstore.auth.security.JwtTokenProvider;
import com.dev24.bookstore.auth.security.RefreshTokenPayload;
import com.dev24.bookstore.auth.security.RefreshTokenStore;
import com.dev24.bookstore.auth.service.AdminService;
import com.dev24.bookstore.auth.service.CustomerService;
import com.dev24.bookstore.auth.service.CustomerSignUpCommand;
import com.dev24.bookstore.common.exception.BusinessException;
import com.dev24.bookstore.common.exception.ErrorCode;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomerService customerService;
    @MockitoBean
    private AdminService adminService;
    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;
    @MockitoBean
    private RefreshTokenStore refreshTokenStore;
    @MockitoBean
    private AccessTokenBlacklist accessTokenBlacklist;

    // 정상 요청이면 회원가입에 성공하고 생성된 고객 정보를 응답으로 반환하는지 검증
    @Test
    void signUpCustomer_returnsCreatedCustomer() throws Exception {
        Customer customer = new Customer("dev24", "hashed", "홍길동", "gildong",
                "gildong@example.com", "010-1234-5678", "서울", "소설", true);
        given(customerService.signUp(new CustomerSignUpCommand("dev24", "password123!", "홍길동", "gildong",
                "gildong@example.com", "010-1234-5678", "서울", "소설", true))).willReturn(customer);

        mockMvc.perform(post("/api/auth/customers/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"loginId":"dev24","password":"password123!","name":"홍길동","nickname":"gildong",
                                 "email":"gildong@example.com","phone":"010-1234-5678","address":"서울",
                                 "interest":"소설","newsletterYn":true}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.loginId").value("dev24"))
                .andExpect(jsonPath("$.data.name").value("홍길동"));
    }

    // 중복 로그인 ID로 가입 시 409 Conflict + A001 에러 코드로 응답하는지 검증
    @Test
    void signUpCustomer_duplicateLoginId_returnsConflict() throws Exception {
        given(customerService.signUp(new CustomerSignUpCommand("dev24", "password123!", "홍길동", "gildong",
                "gildong@example.com", "010-1234-5678", "서울", "소설", true)))
                .willThrow(new BusinessException(ErrorCode.DUPLICATE_LOGIN_ID));

        mockMvc.perform(post("/api/auth/customers/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"loginId":"dev24","password":"password123!","name":"홍길동","nickname":"gildong",
                                 "email":"gildong@example.com","phone":"010-1234-5678","address":"서울",
                                 "interest":"소설","newsletterYn":true}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("A001"));
    }

    // loginId가 빈 문자열이면 @Valid 검증에 걸려 400 + C001로 응답하는지 검증
    @Test
    void signUpCustomer_blankLoginId_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/auth/customers/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"loginId":"","password":"password123!","name":"홍길동","nickname":"gildong",
                                 "email":"gildong@example.com","phone":"010-1234-5678","address":"서울",
                                 "interest":"소설","newsletterYn":true}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("C001"));
    }

    // 비밀번호가 최소 길이(8자) 미만이면 400 + C001로 응답하는지 검증
    @Test
    void signUpCustomer_passwordTooShort_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/auth/customers/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"loginId":"dev24","password":"short","name":"홍길동","nickname":"gildong",
                                 "email":"gildong@example.com","phone":"010-1234-5678","address":"서울",
                                 "interest":"소설","newsletterYn":true}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("C001"));
    }

    // 이메일 형식이 아니면 @Email 검증에 걸려 400 + C001로 응답하는지 검증
    @Test
    void signUpCustomer_invalidEmail_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/auth/customers/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"loginId":"dev24","password":"password123!","name":"홍길동","nickname":"gildong",
                                 "email":"not-an-email","phone":"010-1234-5678","address":"서울",
                                 "interest":"소설","newsletterYn":true}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("C001"));
    }

    // 로그인 요청의 password가 빈 문자열이면 서비스까지 가지 않고 400 + C001로 응답하는지 검증
    @Test
    void loginCustomer_blankPassword_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/auth/customers/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"loginId":"dev24","password":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("C001"));
    }

    // refreshToken이 빈 문자열이면 400 + C001로 응답하는지 검증
    @Test
    void refresh_blankRefreshToken_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"refreshToken":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("C001"));
    }

    // 올바른 자격증명으로 로그인하면 accessToken/refreshToken/tokenType이 응답에 담기는지 검증
    @Test
    void loginCustomer_validCredentials_returnsToken() throws Exception {
        Customer customer = new Customer("dev24", "hashed", "홍길동", "gildong",
                "gildong@example.com", "010-1234-5678", "서울", "소설", true);
        given(customerService.authenticate("dev24", "password123!")).willReturn(customer);
        given(jwtTokenProvider.generateAccessToken("dev24", Role.CUSTOMER)).willReturn("issued-customer-token");
        given(refreshTokenStore.issue("dev24", Role.CUSTOMER)).willReturn("issued-refresh-token");

        mockMvc.perform(post("/api/auth/customers/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"loginId":"dev24","password":"password123!"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("issued-customer-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("issued-refresh-token"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"));
    }

    // 인증 실패 시 서비스가 던진 BusinessException이 401 + A002로 매핑되는지 검증
    @Test
    void loginCustomer_invalidCredentials_returnsUnauthorized() throws Exception {
        given(customerService.authenticate("dev24", "wrongPw"))
                .willThrow(new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        mockMvc.perform(post("/api/auth/customers/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"loginId":"dev24","password":"wrongPw"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("A002"));
    }

    // 관리자 로그인도 고객 로그인과 동일하게 토큰 쌍을 발급받는지 검증
    @Test
    void loginAdmin_validCredentials_returnsToken() throws Exception {
        Admin admin = new Admin("admin01", "hashed", "관리자");
        given(adminService.authenticate("admin01", "adminPw1!")).willReturn(admin);
        given(jwtTokenProvider.generateAccessToken("admin01", Role.ADMIN)).willReturn("issued-admin-token");
        given(refreshTokenStore.issue("admin01", Role.ADMIN)).willReturn("issued-admin-refresh-token");

        mockMvc.perform(post("/api/auth/admins/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"loginId":"admin01","password":"adminPw1!"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("issued-admin-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("issued-admin-refresh-token"));
    }

    // 유효한 리프레시 토큰이면 새 액세스 토큰을 발급하고 리프레시 토큰은 그대로 재사용해 돌려주는지 검증
    @Test
    void refresh_validRefreshToken_returnsNewAccessToken() throws Exception {
        given(refreshTokenStore.find("valid-refresh-token"))
                .willReturn(Optional.of(new RefreshTokenPayload(Role.CUSTOMER, "dev24")));
        given(jwtTokenProvider.generateAccessToken("dev24", Role.CUSTOMER)).willReturn("new-access-token");

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"refreshToken":"valid-refresh-token"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("valid-refresh-token"));
    }

    // Redis에 없는(만료/존재하지 않는) 리프레시 토큰이면 401 + A003으로 응답하는지 검증
    @Test
    void refresh_unknownRefreshToken_returnsUnauthorized() throws Exception {
        given(refreshTokenStore.find("unknown-refresh-token")).willReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"refreshToken":"unknown-refresh-token"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("A003"));
    }

    // 로그아웃 시 현재 액세스 토큰의 jti가 블랙리스트에 등록되고, 리프레시 토큰이 Redis에서 삭제되는지 검증
    @Test
    void logout_blacklistsAccessTokenAndRevokesRefreshToken() throws Exception {
        Date expiration = new Date(System.currentTimeMillis() + 60000L);
        given(jwtTokenProvider.parse("current-access-token"))
                .willReturn(Optional.of(new AccessTokenClaims("dev24", Role.CUSTOMER, "current-jti", expiration)));

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer current-access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"refreshToken":"refresh-token-to-revoke"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(accessTokenBlacklist).blacklist(eq("current-jti"), any());
        verify(refreshTokenStore).revoke("refresh-token-to-revoke");
    }
}