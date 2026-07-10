# JWT 인증 가이드 (액세스 토큰 / 리프레시 토큰 / 블랙리스트)

로그인 시 세션 대신 JWT 액세스 토큰을 발급하고, Redis에 리프레시 토큰을 저장하고, 로그아웃 시 액세스 토큰을 블랙리스트에 올려 강제 무효화하는 구조다. `com.dev24.bookstore.auth.security` 패키지가 핵심이다.

## 한눈에 보는 전체 흐름

```
[회원가입/로그인]
  POST /api/auth/customers/login
        │
        ▼
  CustomerService.authenticate()  ── BCrypt로 비밀번호 검증
        │
        ▼
  JwtTokenProvider.generateAccessToken()   ── HS256 서명, jti 클레임 부여, TTL 15분
  RefreshTokenStore.issue()                ── Redis: refresh-token:{opaque} -> "CUSTOMER:dev24" (TTL 14일)
        │
        ▼
  응답: { accessToken, refreshToken, tokenType: "Bearer" }


[보호된 API 호출]
  요청 헤더: Authorization: Bearer {accessToken}
        │
        ▼
  JwtAuthenticationFilter (매 요청마다 실행)
    1) 서명 검증 (JwtTokenProvider.validateToken)
    2) 블랙리스트 확인 (AccessTokenBlacklist.isBlacklisted)
    3) 둘 다 통과 → SecurityContext에 인증 정보 세팅
        │
        ▼
  SecurityConfig의 authorizeHttpRequests가 permitAll 여부/인증 여부 판단


[토큰 재발급]
  POST /api/auth/refresh  { refreshToken }
        │
        ▼
  RefreshTokenStore.find(refreshToken)  ── Redis 조회로 loginId/role 복원
        │
        ▼
  새 accessToken만 재발급 (refreshToken은 그대로 재사용, 회전 없음)


[로그아웃]
  POST /api/auth/logout  (Authorization 헤더 필요) + { refreshToken }
        │
        ▼
  AccessTokenBlacklist.blacklist(jti, 남은 유효시간)          ── Redis: blacklist:{jti} -> "true"
  RefreshTokenStore.revoke(refreshToken)                      ── Redis: refresh-token:{token} 삭제
```

| 컴포넌트 | 역할 | 의존 자원 |
|---|---|---|
| `JwtTokenProvider` | 액세스 토큰 발급/서명 검증/클레임 추출 | 없음 (순수 `SecretKey` 연산) |
| `JwtAuthenticationFilter` | 매 요청마다 토큰 검증 + `SecurityContext` 세팅 | `JwtTokenProvider`, `AccessTokenBlacklist` |
| `RefreshTokenStore` | 리프레시 토큰 발급/조회/삭제 | Redis |
| `AccessTokenBlacklist` | 로그아웃된 액세스 토큰 등록/조회 | Redis |
| `BearerTokenResolver` | `Authorization: Bearer` 헤더 파싱 | `HttpServletRequest` (서블릿 API) |
| `SecurityConfig` | 필터체인 조립, 경로별 인증 필요 여부 결정 | 위 컴포넌트 전부 |

## 목차

1. 왜 세션이 아니라 JWT(Stateless)인가
2. 액세스 토큰 — 발급/검증과 시크릿 관리
3. 리프레시 토큰 — 왜 JWT가 아니라 opaque 랜덤 문자열인가
4. 로그아웃과 블랙리스트
5. 컴포넌트를 왜 이렇게 나눴는가
6. `SecurityConfig` — 필터체인과 경로별 인증 규칙
7. 에러 코드
8. 직접 실행해보기 (curl)
9. 문제 해결

---

## 1. 왜 세션이 아니라 JWT(Stateless)인가

> **핵심 한 줄**: 서버가 로그인 상태를 세션에 기억하지 않는다. 매 요청은 클라이언트가 들고 있는 JWT로 스스로를 증명해야 한다.

기본값(Spring Security 디폴트)은 로그인 시 `HttpSession`을 만들고 `SecurityContext`를 그 안에 저장한 뒤 `JSESSIONID` 쿠키로 이후 요청을 식별하는 방식이다.<br>
이 프로젝트는 그 대신:

```java
// common/config/SecurityConfig.java
.csrf(csrf -> csrf.disable())                  // Stateless REST API이므로 CSRF는 불필요
.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))  // HttpSession 미생성
```

`STATELESS`로 설정하면 Spring Security는 `HttpSession`을 만들지도, 읽지도 않는다. 클라이언트는 로그인 응답으로 받은 `accessToken`을 저장해뒀다가 이후 모든 요청에 `Authorization: Bearer <token>` 헤더로 직접 실어 보내야 한다.

CSRF를 끈 이유도 같은 맥락이다 — CSRF는 "브라우저가 쿠키를 자동으로 실어 보내는" 세션 기반 인증에서 문제가 되는데, 여기선 쿠키를 아예 안 쓰니 성립하지 않는 공격이다.

세션을 안 쓰면 서버 인스턴스가 로그인 상태를 개별적으로 들고 있지 않아도 돼서, `docker-compose.yml`의 app 2 replica + Nginx 라운드로빈 구조에서도 "어느 인스턴스가 요청을 받든 상관없이" 동작한다.

---

## 2. 액세스 토큰 — 발급/검증과 시크릿 관리

### 2.1 토큰 구조

`auth/security/JwtTokenProvider.java`가 전담한다. 외부 자원(Redis, DB) 의존이 전혀 없는 순수 클래스다 — `SecretKey` 하나만 들고 서명/검증만 한다.

```java
public String generateAccessToken(String loginId, Role role) {
    Date now = new Date();
    Date expiry = new Date(now.getTime() + accessTokenExpirationMs);
    return Jwts.builder()
            .id(UUID.randomUUID().toString())  // jti - 로그아웃 시 블랙리스트 키로 사용
            .subject(loginId)              // 클레임: 로그인 ID
            .claim("role", role.name())    // 클레임: CUSTOMER 또는 ADMIN
            .issuedAt(now)
            .expiration(expiry)
            .signWith(secretKey)           // HS256
            .compact();
}
```

검증과 클레임 추출은 `parse(token)` 하나로 합쳐뒀다:

```java
public Optional<AccessTokenClaims> parse(String token) {
    try {
        Claims claims = parseClaims(token);   // 서명 검증은 여기서 한 번만 일어난다
        return Optional.of(new AccessTokenClaims(
                claims.getSubject(), Role.valueOf(claims.get(ROLE_CLAIM, String.class)),
                claims.getId(), claims.getExpiration()));
    } catch (JwtException | IllegalArgumentException e) {
        return Optional.empty();
    }
}
```

`AccessTokenClaims(loginId, role, jti, expiration)`는 이 프로젝트가 정의한 record다 — jjwt의 `Claims` 타입을 호출하는 쪽(필터, 컨트롤러)에 그대로 노출하지 않으려고 감싼 것이다. 서명이 틀렸거나, 만료됐거나, 형식이 깨진 문자열이면 빈 `Optional`을 반환한다.

**왜 `validateToken`/`getLoginId`/`getRole`/`getJti`/`getExpiration`처럼 따로따로 만들지 않았나**:<br>
jti를 도입하기 전에는 실제로 그렇게 나뉘어 있었는데, `JwtAuthenticationFilter`가 토큰 하나로 이 메서드들을 3~4번 연달아 호출하면서 **서명 검증(HMAC 연산)을 매번 다시** 하고 있었다.<br>
`parseClaims`(내부에서 실제 파싱하는 메서드)는 애초에 보안상 이유가 아니라, jjwt의 `Claims` 타입을 감추려는 캡슐화 목적으로만 `private`이었다.<br>
— 그래서 굳이 여러 개로 안 쪼개고, 한 번 파싱해서 필요한 값을 전부 담은 `AccessTokenClaims`를 반환하는 `parse()` 하나로 합쳤다. `jti`는 그 값들 중 하나이자 블랙리스트의 키가 된다(4절 참고).

### 2.2 만료 시간

```properties
# application.properties
app.jwt.access-token-expiration-ms=900000   # 15분
```

액세스 토큰은 짧게 가져간다 — 탈취당해도 피해 시간을 최소화하려는 목적이다. (로그인을 자주 다시 하지 않아도 되게 해주는 게 리프레시 토큰의 역할, 3절 참고.)

### 2.3 시크릿(서명 키) 관리

```java
public JwtTokenProvider(
        @Value("${app.jwt.secret}") String secret,
        @Value("${app.jwt.access-token-expiration-ms}") long accessTokenExpirationMs) {
    this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    ...
```

`app.jwt.secret`은 Base64로 인코딩된 값을 받아 디코딩한 뒤 `SecretKey`를 만든다. HS256은 최소 256비트(32바이트)가 필요하다 — 짧은 문자열을 그대로 쓰면 `WeakKeyException`이 난다.

**실제 시크릿 값과 커밋 안전성이 중요하다.**<br>
`application.properties`에 있는 값은 진짜 랜덤 키가 아니라 base64로 감싼 사람이 읽을 수 있는 문구다:

```properties
# JWT - this is an obviously-fake placeholder, safe to commit. Any real environment (docker-compose, CI, prod)
# MUST override it via the APP_JWT_SECRET env var (see .env.example) - a real key must never be committed here.
app.jwt.secret=bG9jYWwtZGV2LXBsYWNlaG9sZGVyLWRvLW5vdC11c2UtaW4tcHJvZHVjdGlvbiEh
```

디코딩하면 `local-dev-placeholder-do-not-use-in-production!!`가 그대로 나온다.

**왜 진짜 랜덤 값을 커밋하면 안 되는가**:<br>
JWT 시크릿이 유출되면 공격자가 서버에 한 번도 접속하지 않고 오프라인에서 임의 계정(예: 관리자)으로 서명된 토큰을 위조할 수 있다. DB 자격증명 유출보다 더 치명적이다 — DB는 최소한 네트워크 접근이 필요한데, 시크릿은 알기만 하면 끝이다.

실제 배포/도커 환경에서는 반드시 환경변수로 덮어써야 한다:

```yaml
# docker-compose.yml
environment:
  APP_JWT_SECRET: ${APP_JWT_SECRET:?APP_JWT_SECRET is not set - copy .env.example to .env and fill it in}
```

`${VAR:?메시지}`는 셸 문법을 그대로 차용한 Docker Compose의 "필수 변수" 문법이다 — `APP_JWT_SECRET`이 없으면 `docker compose up` 자체가 그 메시지를 띄우며 실패한다.

실제 값은 `.env`(`.gitignore`에 등록됨)에만 두고, `.env.example`(커밋됨, 키 이름만 있고 값은 빈 템플릿)로 다른 개발자에게 안내한다:

```
# .env.example
APP_JWT_SECRET=
```

```bash
# 새 시크릿 생성 방법
openssl rand -base64 32
```

**주의할 점 — 실행 방식을 섞으면 토큰이 안 맞는다.**<br>
`.env`는 Docker Compose 툴 자체가 읽는 파일이라, `./gradlew bootRun`으로 로컬에서 직접 띄우면 Spring Boot는 이 파일의 존재를 모른다(`application.properties`의 placeholder를 그대로 씀). 그래서:
- `docker-compose up` 전체를 띄우면 → 2개 replica 모두 `.env`의 같은 진짜 시크릿을 쓰므로 서로 토큰 호환됨, 문제 없음.
- `docker-compose up postgres redis nats`(인프라만) 후 로컬 `bootRun` → 앱 인스턴스가 하나뿐이라 자기 자신과는 항상 일치, 문제 없음.
- 이 둘을 **동시에** 띄우면(도커의 `app` + 로컬 `bootRun`)<br>
→ 서로 다른 시크릿을 쓰는 두 인스턴스가 되어, 한쪽에서 발급한 토큰이 다른 쪽에서 검증 실패한다. 맞추려면 로컬 셸에서도 `.env`와 같은 값을 `APP_JWT_SECRET`으로 export해야 한다.
하지만 재현될 가능성이 희박함.

### 2.4 왜 HS256을 써도 안전한가 (다중 서비스 환경 대응 포함)

> **핵심 한 줄**: "HS256 취약점"으로 검색되는 사례들은 우리 구현에 해당하지 않는다. 다중 서비스 환경에서 진짜 문제가 되는 건 키 공유인데, 이건 게이트웨이 중앙 검증(엣지 인증 패턴, MSA에서 흔히 쓰는 방식)으로 해결한다.

HS256 취약점 사례는 대부분 다음 셋 중 하나다.

1. **알고리즘 혼동 공격(RS256→HS256)** — 검증 코드가 토큰 헤더의 `alg`를 그대로 믿고 검증 방식을 정하면, 공격자가 `alg: HS256`으로 바꿔 서버의 RSA 공개키를 HMAC 비밀키처럼 재사용해 위조할 수 있다.
   → 해당 없음: RS256을 안 쓰고, jjwt의 `verifyWith(secretKey)`는 검증 키/알고리즘을 코드에서 고정한다.
2. **`alg: none` 공격** — 서명 자체를 생략해 검증을 우회한다. jjwt는 기본적으로 거부한다.
3. **약한 시크릿 브루트포스** — 대칭키라 시크릿이 짧으면 오프라인 브루트포스가 가능하다.
   → `Keys.hmacShaKeyFor`가 256비트 미만은 예외를 던지고, 실제 시크릿은 `openssl rand -base64 32`로 생성한다(2.3절).

**진짜 트레이드오프는 다중 서비스 환경에서의 키 공유 문제다.**<br>
HS256은 서명과 검증에 같은 키를 쓴다. 인증 모듈이 별도 서비스로 분리되고 다른 서비스들(도서/장바구니/리뷰 등)이 각자 프로세스에서 토큰을 검증해야 한다면, 시크릿을 모든 서비스에 나눠줘야 하고 그중 하나라도 뚫리면 임의 사용자로 토큰을 위조할 수 있게 된다.

**해결책 — 게이트웨이에서 중앙 검증한다.**<br>
검증을 **게이트웨이 한 곳에서만** 하고 뒷단 서비스들은 각자 토큰을 다시 검증하지 않는 구조(엣지 인증 패턴, MSA에서 흔히 쓰는 방식)다.

```
클라이언트 → [게이트웨이: JWT 검증] → 내부망 → [서비스A, 서비스B, ...]
                                              (X-User-Id, X-User-Role 같은 헤더로 신원 전달)
```

이러면 HS256 시크릿은 게이트웨이라는 **딱 한 프로세스**에만 있으면 되므로, "여러 서비스가 같은 시크릿을 나눠 가져야 하는" 문제 자체가 사라진다 — 지금 이 프로젝트(단일 앱이 발급·검증을 다 함)와 본질적으로 같은 상황이 된다. 다만 문제가 없어지는 게 아니라 다른 곳으로 옮겨갈 뿐이다:

- **뒷단 서비스가 게이트웨이를 거치지 않고 외부에서 직접 접근 가능하면 안 된다.**
- **뒷단 서비스가 그 신원 헤더를 무조건 신뢰해도 되는 근거가 있어야 한다.**

---

## 3. 리프레시 토큰 — 왜 JWT가 아니라 opaque 랜덤 문자열인가

`auth/security/RefreshTokenStore.java`가 전담하며, Redis에 의존한다.

```java
public String issue(String loginId, Role role) {
    String refreshToken = generateOpaqueToken(); // SecureRandom 32바이트 → Base64URL
    String value = role.name() + ":" + loginId;
    redisTemplate.opsForValue().set(KEY_PREFIX + refreshToken, value, Duration.ofMillis(refreshTokenExpirationMs));
    return refreshToken;
}
```

**JWT로 안 만든 이유**:<br>
리프레시 토큰은 어차피 Redis에 저장해야 한다(로그아웃 시 삭제해서 무효화해야 하니까).<br>
그럴 거면 서명 검증 같은 JWT의 장점(자체 검증 가능한 stateless 구조)을 살릴 이유가 없다.<br>
그냥 `SecureRandom`으로 추측 불가능한 문자열을 만들고, Redis 조회 한 번으로 신원을 복원하는 게 더 단순하다:

```java
public Optional<RefreshTokenPayload> find(String refreshToken) {
    String value = redisTemplate.opsForValue().get(KEY_PREFIX + refreshToken);
    if (value == null) return Optional.empty();
    String[] parts = value.split(":", 2);
    return Optional.of(new RefreshTokenPayload(Role.valueOf(parts[0]), parts[1]));
}
```

Redis 키/값 구조: `refresh-token:{opaque토큰} -> "CUSTOMER:dev24"` (TTL = `app.jwt.refresh-token-expiration-ms`, 14일).

**토큰 회전(rotation)은 구현하지 않았다.**<br>
`/api/auth/refresh`를 호출해도 새 리프레시 토큰을 발급하지 않고 같은 토큰을 계속 재사용한다(만료되거나 로그아웃으로 삭제되기 전까지). 매 갱신마다 회전시키면 "이전 리프레시 토큰이 다시 쓰이면 탈취 신호로 간주" 같은 탐지가 가능해지지만, 이 프로젝트 범위에서는 일단 저장+로그아웃 무효화까지만 구현하기로 했다.

`revoke(refreshToken)`은 단순히 Redis 키를 삭제한다 — 로그아웃 시 호출된다(4절).

---

## 4. 로그아웃과 블랙리스트

`auth/security/AccessTokenBlacklist.java`가 전담하며, Redis에 의존한다.

**왜 필요한가**:<br>
JWT 액세스 토큰은 stateless라 서버가 "이 토큰 아직 유효해?"를 자체 검증(서명 확인)만으로 판단한다. 즉 로그아웃해도 자연 만료 시각까지는 여전히 유효한 서명을 가진 토큰이다. 로그아웃을 진짜로 의미 있게 만들려면, "발급은 됐지만 강제로 막고 싶은" 토큰 목록이 별도로 필요하다 — 그게 블랙리스트다.

```java
public void blacklist(String jti, Duration ttl) {
    if (ttl.isZero() || ttl.isNegative()) return;   // 이미 만료된 토큰은 저장할 필요 없음
    redisTemplate.opsForValue().set(KEY_PREFIX + jti, "true", ttl);
}

public boolean isBlacklisted(String jti) {
    return redisTemplate.hasKey(KEY_PREFIX + jti);
}
```

**블랙리스트 키는 `jti`(JWT ID)다.**<br>
`JwtTokenProvider.generateAccessToken`이 토큰 발급 시 `.id(UUID.randomUUID().toString())`로 매 토큰마다 고유한 `jti` 클레임을 심어두고, `parse(token)`이 반환하는 `AccessTokenClaims.jti()`로 이를 꺼낸다. 토큰 원문(200~300자)을 그대로 키로 쓸 수도 있었지만, `jti`(UUID, 36자)를 쓰면 Redis 키 크기가 훨씬 작고 표준적이다.

TTL을 **토큰의 남은 자연 수명**으로 맞추는 게 핵심이다 — `AuthController.logout`에서:

```java
AccessTokenClaims claims = jwtTokenProvider.parse(accessToken).orElseThrow(...);
Duration remainingTtl = Duration.between(Instant.now(), claims.expiration().toInstant());
accessTokenBlacklist.blacklist(claims.jti(), remainingTtl);
```

이렇게 하면 Redis 항목이 토큰 자연 만료 시점에 자동으로 사라진다 — 굳이 스케줄러로 청소할 필요가 없다. 반영구적으로 쌓이지 않는 이유가 여기 있다.

`JwtAuthenticationFilter`는 서명 검증을 통과한 토큰도 그 `jti`가 블랙리스트에 있으면 인증 처리를 하지 않는다:

```java
jwtTokenProvider.parse(token)
        .filter(claims -> !accessTokenBlacklist.isBlacklisted(claims.jti()))
        .ifPresent(claims -> { ...SecurityContext에 인증 정보 세팅... });
```

(2.1절에서 다뤘듯 `parse()`가 서명 검증 + 클레임 추출을 한 번에 하므로, 토큰 하나당 파싱은 정확히 한 번만 일어난다.)

**로그아웃 엔드포인트는 인증이 필요하다**<br>
`/api/auth/logout`은 `SecurityConfig`의 permitAll 목록에서 빠져 있다(6절). 유효한 액세스 토큰이 있어야만 호출 가능하다는 뜻이고, 그래서 컨트롤러 진입 시점에 "블랙리스트에 올릴 토큰이 항상 존재한다"는 게 보장된다.

**탈취 대응의 한계**:<br>
블랙리스트는 "누군가 탈취를 알아채야" 동작한다. 아무도 눈치 못 채면 만료 전까지는 그대로 유효하다. 그래서 액세스 토큰 TTL을 짧게(15분) 가져가는 것과, 탈취 감지 즉시 액세스 토큰 블랙리스트 등록 + 리프레시 토큰 삭제(전체 재로그인 강제)를 함께 쓰는 게 현실적인 방어선이다.

---

## 5. 컴포넌트를 왜 이렇게 나눴는가

기준은 **"이 로직이 어떤 외부 자원에 의존하는가"**다.

- **`JwtTokenProvider`**: 외부 자원 의존 없음. `SecretKey`만으로 서명/검증하는 순수 stateless 코덱. Redis가 없는 다른 프로젝트에 그대로 재사용할 수 있고, 단위 테스트에 Redis mock이 전혀 필요 없다(`JwtTokenProviderTest` 참고).
- **`RefreshTokenStore` / `AccessTokenBlacklist`**: 둘 다 Redis 의존이지만 서로 다른 이유로 존재한다 — 하나는 "JWT가 아닌 별도 토큰(리프레시)"을 관리하고, 하나는 "이미 발급된 JWT를 무효화하는 목록"을 관리한다. 키 구조도, 저장하는 값도, 존재 목적도 다르다.
- **`BearerTokenResolver`**: `HttpServletRequest`(서블릿 API) 의존. 사실 "토큰"과 무관한 순수 HTTP 헤더 파싱이라 JWT 로직과 섞을 이유가 없다. `JwtAuthenticationFilter`(매 요청 검증)와 `AuthController.logout`(현재 토큰 추출) 양쪽에서 재사용된다.

한 클래스에 다 몰아넣으면 "암호화 + Redis I/O + 서블릿 헤더 파싱"이라는 무관한 기술적 관심사가 뒤섞여서, 클레임 구조 하나 바꾸려 해도 Redis 코드까지 같이 열어봐야 하고, JWT 파싱 로직 테스트에도 항상 Redis mock을 세팅해야 하는 문제가 생긴다. 지금처럼 나누면 클래스 이름만 봐도 "왜 존재하는지", "뭐가 바뀌면 고쳐야 하는지"가 바로 보인다.

---

## 6. `SecurityConfig` — 필터체인과 경로별 인증 규칙

```java
private static final String[] PERMIT_ALL_PATHS = {
        "/api/auth/customers/signup",
        "/api/auth/customers/login",
        "/api/auth/admins/login",
        "/api/auth/refresh",
        "/swagger-ui/**",
        "/v3/api-docs/**",
        "/actuator/health"
};

.authorizeHttpRequests(auth -> auth
        .requestMatchers(PERMIT_ALL_PATHS).permitAll()
        .anyRequest().authenticated())
.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
```

`/api/auth/**`를 통째로 열어두지 않고 회원가입/로그인/리프레시만 명시했다 — **`/api/auth/logout`은 의도적으로 제외**했다(4절 참고, 로그아웃은 인증된 토큰이 있어야 호출 가능해야 하므로).

`addFilterBefore`로 `JwtAuthenticationFilter`를 `UsernamePasswordAuthenticationFilter`(폼 로그인용 기본 필터) 앞에 끼워 넣었다 — 이 필터는 permitAll 여부와 무관하게 **모든 요청에서 항상 실행**된다.<br>
토큰이 유효하면 `SecurityContext`를 채워두고, 그 뒤에 `authorizeHttpRequests`가 "이 경로가 permitAll인지 / 인증이 있는지"를 최종 판단한다.<br>
그래서 permitAll 경로라도 유효한 토큰을 들고 오면 인증 정보 자체는 채워진다(예: `/api/auth/refresh`가 permitAll이어도, 그 안에서 만약 `SecurityContext`를 쓰고 싶다면 쓸 수 있다).

---

## 7. 에러 코드

`common/exception/ErrorCode.java`에 인증 도메인 코드를 `A0xx` 프리픽스로 모아뒀다(공통 코드는 `C0xx`).

| 코드 | HTTP 상태 | 상황 |
|---|---|---|
| `A001` | 409 Conflict | 회원가입 시 이미 존재하는 로그인 ID |
| `A002` | 401 Unauthorized | 로그인 실패(아이디 없음/비밀번호 불일치 — 사용자에게는 구분 없이 동일하게 응답, 계정 존재 여부 노출 방지) |
| `A003` | 401 Unauthorized | `/api/auth/refresh`에 존재하지 않거나 만료된 리프레시 토큰을 보낸 경우 |

---

## 8. 직접 실행해보기 (curl)

`docker-compose up`으로 전체 스택을 띄우거나, `docker-compose up postgres redis nats` 후 `./gradlew bootRun`으로 로컬 실행한 상태를 가정한다(포트는 8080).

```bash
# 1-1. 회원가입
curl -s -X POST http://localhost:8080/api/auth/customers/signup \
  -H "Content-Type: application/json" \
  -d '{"loginId":"dev24","password":"password123!","name":"Gildong","nickname":"gildong","email":"gildong@example.com","phone":"010-1234-5678","address":"Seoul","interest":"Novel","newsletterYn":true}'

# 1-2. 회원가입(한글 포함) - 명령줄 인자 대신 파일로 전달해 인코딩 깨짐 방지(9절 참고)
cat > signup.json << 'EOF'
{"loginId":"dev24","password":"password123!","name":"홍길동","nickname":"gildong","email":"gildong@example.com","phone":"010-1234-5678","address":"서울","interest":"소설","newsletterYn":true}
EOF
curl -s -X POST http://localhost:8080/api/auth/customers/signup \
  -H "Content-Type: application/json" \
  --data-binary @signup.json

# 2. 로그인 - accessToken / refreshToken 발급받기
curl -s -X POST http://localhost:8080/api/auth/customers/login \
  -H "Content-Type: application/json" \
  -d '{"loginId":"dev24","password":"password123!"}'
# 응답 예: {"success":true,"data":{"accessToken":"eyJ...","refreshToken":"AbCd...","tokenType":"Bearer"}}

ACCESS_TOKEN="위 응답의 accessToken 값"
REFRESH_TOKEN="위 응답의 refreshToken 값"

# 3. 액세스 토큰으로 재발급 시도 (참고용 - refresh 자체는 인증 불필요)
curl -s -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d "{\"refreshToken\":\"$REFRESH_TOKEN\"}"

# 4. 로그아웃 - Authorization 헤더 필수(빠뜨리면 401)
curl -s -X POST http://localhost:8080/api/auth/logout \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"refreshToken\":\"$REFRESH_TOKEN\"}"

# 5. 로그아웃한 리프레시 토큰으로 재발급 시도 - A003(401)이어야 정상
curl -s -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d "{\"refreshToken\":\"$REFRESH_TOKEN\"}"

# 6. Redis에서 직접 확인
docker compose exec redis redis-cli --scan --pattern "refresh-token:*"
docker compose exec redis redis-cli --scan --pattern "blacklist:*"
```

PowerShell에서는 `curl` 대신 `curl.exe`를 쓰고, 작은따옴표 JSON 대신 여기서처럼 이스케이프된 큰따옴표를 쓰거나 `Invoke-RestMethod`를 쓰면 된다.

---

## 9. 문제 해결

| 증상 | 원인 / 해결                                                                                                                                                                                                                                                                                                         |
|---|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| security 스타터 추가 후 Swagger/Actuator까지 401 | `SecurityConfig`의 `PERMIT_ALL_PATHS`에 경로가 빠졌는지 확인. `spring-boot-starter-security`는 기본적으로 모든 요청에 인증을 요구한다.                                                                                                                                                                                                       |
| 로그인은 되는데 그 토큰으로 아무 요청이나 401 | 정상일 수 있다 — 현재 `/api/auth/**`(로그아웃 제외) 외에는 보호할 실제 비즈니스 엔드포인트가 아직 없다(이후 모듈에서 추가 예정).                                                                                                                                                                                                                              |
| `docker compose up` 하자마자 `APP_JWT_SECRET is not set` 에러로 실패 | `.env.example`을 `.env`로 복사하고 `openssl rand -base64 32`로 값을 채워야 한다.                                                                                                                                                                                                                                              |
| `BCryptPasswordEncoder`가 `IllegalArgumentException`을 던짐 | 비밀번호가 72바이트를 초과했다. `CustomerSignUpRequest.password`에 `@Size(max = 72)`가 있어 정상 흐름에서는 그 전에 400으로 걸러진다 — DTO 검증을 우회해서 서비스로 직접 호출한 경우일 것이다.                                                                                                                                                                         |
| `WeakKeyException: The signing key's size is ... bits which is not secure enough` | `app.jwt.secret`이 Base64로 디코딩했을 때 32바이트(256비트) 미만이다. `openssl rand -base64 32`로 다시 생성한다.                                                                                                                                                                                                                        |
| 리프레시 토큰으로 재발급했는데 다음 재발급도 똑같은 토큰이 반환됨 | 버그가 아니다 — 이 프로젝트는 리프레시 토큰 회전을 구현하지 않았다(3절 참고). 로그아웃 전까지는 같은 리프레시 토큰이 계속 유효하다.                                                                                                                                                                                                                                   |
| curl로 회원가입 요청 시 `{"type":"about:blank","title":"Bad Request",...,"detail":"Failed to read request",...}` 응답(우리 `ApiResponse` 형식이 아님) | JSON 바디에 포함된 한글(이름/주소 등)이 curl 명령줄 인자로 전달되는 과정에서 UTF-8이 아닌 인코딩으로 깨져, Jackson이 JSON 파싱 자체를 실패한 것이다.<br>(Bean Validation까지 못 감, 그래서 우리 커스텀 에러 포맷 대신 Spring 기본 RFC 7807 포맷이 나온다)<br> 한글을 뺀 영문 전용 바디로 테스트했을 때 성공하면 확정이다.<br> JSON을 UTF-8로 저장한 파일을 만들어 `curl --data-binary @signup.json`으로 보내면 명령줄 인자 인코딩 변환 과정을 건너뛰어 해결된다. |
