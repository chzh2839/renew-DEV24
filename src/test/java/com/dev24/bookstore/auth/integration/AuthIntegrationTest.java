package com.dev24.bookstore.auth.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.dev24.bookstore.auth.domain.Admin;
import com.dev24.bookstore.auth.repository.AdminRepository;
import com.dev24.bookstore.auth.repository.CustomerRepository;
import com.dev24.bookstore.auth.security.AccessTokenClaims;
import com.dev24.bookstore.auth.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

// 실제 Postgres(Flyway 마이그레이션 포함)와 실제 Redis를 Testcontainers로 띄워 인증 흐름을 end-to-end로 검증한다.
// 나머지 인증 테스트(AuthControllerTest 등)는 전부 의존성을 목킹하므로,
// 실제 Redis 기반 블랙리스트 체크 분기를 통과하는 테스트는 이 클래스가 유일하다.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
class AuthIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private AdminRepository adminRepository;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.jwt.refresh-token-expiration-ms}")
    private long refreshTokenExpirationMs;

    private static String uniqueLoginId(String prefix) {
        return "itest-" + prefix + "-" + UUID.randomUUID();
    }

    private String signUpCustomerJson(String loginId) {
        return """
                {"loginId":"%s","password":"password123!","name":"홍길동","nickname":"gildong",
                 "email":"%s@example.com","phone":"010-1234-5678","address":"서울",
                 "interest":"소설","newsletterYn":true}
                """.formatted(loginId, loginId);
    }

    // 회원가입 성공 시 실제 Postgres에 저장되고 비밀번호가 평문이 아닌 BCrypt 해시로 저장되는지 검증
    @Test
    void signUpCustomer_persistsToPostgresWithHashedPassword() throws Exception {
        String loginId = uniqueLoginId("signup");

        mockMvc.perform(post("/api/auth/customers/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signUpCustomerJson(loginId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.loginId").value(loginId));

        var saved = customerRepository.findByLoginId(loginId).orElseThrow();
        assertThat(saved.getPasswordHash()).isNotEqualTo("password123!");
        assertThat(passwordEncoder.matches("password123!", saved.getPasswordHash())).isTrue();
    }

    // 동일 loginId로 재가입 시 실제 DB unique 제약에 의해 409+A001로 응답하는지 검증
    @Test
    void signUpCustomer_duplicateLoginId_returnsConflict() throws Exception {
        String loginId = uniqueLoginId("dup");

        mockMvc.perform(post("/api/auth/customers/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signUpCustomerJson(loginId)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/customers/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signUpCustomerJson(loginId)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("A001"));
    }

    // 로그인 성공 시 액세스 토큰이 발급되고 리프레시 토큰이 실제 Redis에 올바른 키/값/TTL로 저장되는지 검증
    @Test
    void loginCustomer_issuesAccessTokenAndPersistsRefreshTokenInRedis() throws Exception {
        String loginId = uniqueLoginId("login");
        mockMvc.perform(post("/api/auth/customers/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signUpCustomerJson(loginId)))
                .andExpect(status().isOk());

        String responseBody = mockMvc.perform(post("/api/auth/customers/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"loginId":"%s","password":"password123!"}
                                """.formatted(loginId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists())
                .andReturn().getResponse().getContentAsString();

        JsonNode data = objectMapper.readTree(responseBody).get("data");
        String refreshToken = data.get("refreshToken").asText();

        String redisKey = "refresh-token:" + refreshToken;
        assertThat(redisTemplate.opsForValue().get(redisKey)).isEqualTo("CUSTOMER:" + loginId);
        Long ttlSeconds = redisTemplate.getExpire(redisKey);
        assertThat(ttlSeconds).isGreaterThan(0);
        assertThat(Duration.ofSeconds(ttlSeconds)).isLessThanOrEqualTo(Duration.ofMillis(refreshTokenExpirationMs));
    }

    // 관리자 로그인도 동일한 흐름으로 토큰이 발급되며 Redis 값에 ADMIN 역할 접두사가 붙는지 검증
    @Test
    void loginAdmin_issuesTokenWithAdminRolePrefixInRedis() throws Exception {
        String loginId = uniqueLoginId("admin");
        adminRepository.save(new Admin(loginId, passwordEncoder.encode("adminPw123!"), "관리자"));

        String responseBody = mockMvc.perform(post("/api/auth/admins/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"loginId":"%s","password":"adminPw123!"}
                                """.formatted(loginId)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String refreshToken = objectMapper.readTree(responseBody).get("data").get("refreshToken").asText();
        assertThat(redisTemplate.opsForValue().get("refresh-token:" + refreshToken)).isEqualTo("ADMIN:" + loginId);
    }

    // 유효한 리프레시 토큰으로 재발급 요청 시 새 액세스 토큰이 발급되고 리프레시 토큰은 그대로 재사용되는지 검증
    @Test
    void refresh_validToken_issuesNewAccessTokenAndReusesRefreshToken() throws Exception {
        String loginId = uniqueLoginId("refresh");
        mockMvc.perform(post("/api/auth/customers/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signUpCustomerJson(loginId)))
                .andExpect(status().isOk());

        String loginResponseBody = mockMvc.perform(post("/api/auth/customers/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"loginId":"%s","password":"password123!"}
                                """.formatted(loginId)))
                .andReturn().getResponse().getContentAsString();
        JsonNode loginData = objectMapper.readTree(loginResponseBody).get("data");
        String originalAccessToken = loginData.get("accessToken").asText();
        String refreshToken = loginData.get("refreshToken").asText();

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"refreshToken":"%s"}
                                """.formatted(refreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value(org.hamcrest.Matchers.not(originalAccessToken)))
                .andExpect(jsonPath("$.data.refreshToken").value(refreshToken));
    }

    // Redis에 없는 리프레시 토큰으로 요청 시 401+A003으로 거부되는지 검증
    @Test
    void refresh_unknownToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"refreshToken":"unknown-refresh-token"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("A003"));
    }

    // 로그아웃 시 액세스 토큰 jti가 실제 Redis 블랙리스트에 등록되고 리프레시 토큰이 삭제되며,
    // 이후 같은(블랙리스트된) 토큰으로의 요청이 실제 필터 체인에 의해 거부되는지 검증
    @Test
    void logout_blacklistsAccessTokenAndRevokesRefreshTokenInRedis_thenSubsequentRequestIsRejected() throws Exception {
        String loginId = uniqueLoginId("logout");
        mockMvc.perform(post("/api/auth/customers/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signUpCustomerJson(loginId)))
                .andExpect(status().isOk());

        String loginResponseBody = mockMvc.perform(post("/api/auth/customers/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"loginId":"%s","password":"password123!"}
                                """.formatted(loginId)))
                .andReturn().getResponse().getContentAsString();
        JsonNode loginData = objectMapper.readTree(loginResponseBody).get("data");
        String accessToken = loginData.get("accessToken").asText();
        String refreshToken = loginData.get("refreshToken").asText();

        AccessTokenClaims claims = jwtTokenProvider.parse(accessToken).orElseThrow();

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"refreshToken":"%s"}
                                """.formatted(refreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        assertThat(redisTemplate.hasKey("blacklist:" + claims.jti())).isTrue();
        assertThat(redisTemplate.hasKey("refresh-token:" + refreshToken)).isFalse();

        // 블랙리스트에 등록된 토큰으로는 실제 JwtAuthenticationFilter를 통과하지 못하고 인증되지 않은 요청으로 거부되어야 한다.
        // URL 레벨 .anyRequest().authenticated()(AuthorizationFilter)가 DispatcherServlet에 도달하기 전에 막으므로,
        // Spring Security의 기본 AccessDeniedHandler가 응답하며 본문 없이 403만 내려온다 - GlobalExceptionHandler(A004 JSON 본문)는
        // @PreAuthorize AOP 계층에서만 개입하는데, 이 경로는 그 이전에 이미 차단된다.
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"refreshToken":"%s"}
                                """.formatted(refreshToken)))
                .andExpect(status().isForbidden());
    }
}
