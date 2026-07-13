# 도서 카탈로그 캐싱 가이드 (Redis 기반 Spring Cache `@Cacheable`)

도서 목록(`GET /api/books`)/상세(`GET /api/books/{id}`) 조회 결과를 Redis에 캐싱해 반복 조회 시 DB를 건너뛰는 구조다. `com.dev24.bookstore.book` 패키지(`config`/`service`/`controller`/`repository`)가 핵심이다.

## 한눈에 보는 전체 흐름

```
[목록 검색]                                    [상세 조회]
GET /api/books?keyword=...                     GET /api/books/{id}
        │                                               │
        ▼                                               ▼
BookController.search()                        BookController.detail()
        │                                               │
        ▼                                               ▼
BookQueryService.search(condition, pageable)   BookQueryService.getDetail(id)
   @Cacheable("bookSearch")                       @Cacheable("bookDetail")
        │                                               │
        ├─ Redis에 같은 키 있음? ──── Yes ────────────────┤──────────────────────→ 메서드 실행 안 하고 캐시값 반환 (DB 쿼리 0건)
        │                                               │
        No (캐시 미스)                                  No (캐시 미스)
        │                                               │
        ▼                                               ▼
BookRepository.search()                        BookRepository.findByIdWithDetails(id)
  QueryDSL 동적 검색 + fetch join                 JPQL 고정 쿼리 + fetch join
  (book_image/rating LEFT JOIN FETCH)             (book_image/rating LEFT JOIN FETCH)
        │                                               │
        ▼                                               ▼
BookSearchResult(content, totalElements)       BookResponse
  로 변환해서 리턴 → Redis에 JSON으로 저장         로 변환해서 리턴 → Redis에 JSON으로 저장
        │                                               │
        ▼                                               ▼
BookController가 Pageable과 합쳐                응답
Page<BookResponse>로 재구성 후 응답
```

| 컴포넌트 | 역할 | 의존 자원 |
|---|---|---|
| `BookCacheConfig` | `@EnableCaching` + 캐시별(`bookSearch`/`bookDetail`) Redis 직렬화 설정(TTL 10분) | Redis |
| `BookQueryService` | `search()`/`getDetail()`에 `@Cacheable` 적용, 엔티티 → DTO 변환 | `BookRepository`, Redis(via `CacheManager`) |
| `BookSearchResult` | 목록 검색의 캐시 "값" — `content`(List\<BookResponse\>) + `totalElements` | 없음 (순수 record) |
| `BookRepository.findByIdWithDetails` | 상세 조회용 고정 fetch-join 쿼리(JPQL `@Query`) | Postgres |
| `BookController` | HTTP 계층 — 캐시 값(`BookSearchResult`)을 `Page<BookResponse>`로 재구성, 상세 엔드포인트 라우팅 | `BookQueryService` |

## 목차

1. [왜 `StringRedisTemplate` 대신 Spring Cache(`@Cacheable`)인가](#1-왜-stringredistemplate-대신-spring-cacheCacheable인가)
2. [캐시 값 설계 — 왜 엔티티도 `Page`도 아닌가](#2-캐시-값-설계--왜-엔티티도-page도-아닌가)
3. [Jackson 직렬화 함정과 해결](#3-jackson-직렬화-함정과-해결)
4. [캐시 키 생성](#4-캐시-키-생성)
5. [상세 조회 엔드포인트](#5-상세-조회-엔드포인트)
6. [테스트 전략](#6-테스트-전략)
7. [캐시 히트/미스 응답시간 실측](#7-캐시-히트미스-응답시간-실측)
8. [직접 실행해보기](#8-직접-실행해보기)
9. [범위 밖으로 남긴 것](#9-범위-밖으로-남긴-것)

## 1. 왜 `StringRedisTemplate` 대신 Spring Cache(`@Cacheable`)인가

인증 모듈(`RefreshTokenStore`, `AccessTokenBlacklist`, `docs/JWT.md` 참고)은 이미 Redis를 쓰고 있지만 `StringRedisTemplate`으로 **직접** 다룬다:
```java
redisTemplate.opsForValue().set(KEY_PREFIX + refreshToken, value, Duration.ofMillis(ttl));
```
이건 "문자열 하나를 정해진 키 포맷으로 저장/조회/삭제"하는 단순한 key-value 작업이라 직접 다루는 게 자연스럽다.

반면 도서 조회는 "같은 조건으로 또 조회하면 DB 안 타고 캐시에서 바로 준다"는 전형적인 **메서드 결과 캐싱** 패턴이다.<br>
`StringRedisTemplate`으로 직접 하면 메서드마다 "키 조합 → 캐시 확인 → 없으면 실행 후 저장"을 손으로 반복해야 한다.<br>
`@Cacheable`은 정확히 이 반복 패턴을 프레임워크가 대신 해준다 — 캐시 키는 메서드 파라미터에서 자동 생성되고, "확인 → 없으면 실행 → 저장" 흐름 자체가 코드에 안 보인다(선언적).<br>
뒤에서 실제로 값을 저장하는 곳은 여전히 Redis(`RedisCacheManager`를 통해) — 인프라가 바뀌는 게 아니라 그걸 다루는 보일러플레이트가 없어지는 것.


## 2. 캐시 값 설계 — 왜 엔티티도 `Page`도 아닌가

**JPA 엔티티(`Book` 등)를 캐시 값으로 두면 안 된다.**<br>
`Book`/`BookImage`/`Rating`은 `Serializable`이 아니고, 지연 로딩 프록시·영속성 컨텍스트 참조를 갖고 있어 그대로 캐싱하면 위험하다(직렬화 실패 또는 세션이 끊긴 프록시 역직렬화 문제). 그래서 캐시 값은 항상 DTO(`BookResponse`)다.

**`Page`/`PageImpl`도 캐시 값으로 두지 않는다.**<br>
`Page`는 기본 생성자가 없고 Jackson으로 깔끔하게 직렬화/역직렬화되도록 설계된 타입이 아니라 Redis JSON 캐싱에 부적합하다. 대신 `content`+`totalElements`만 담는 경량 record를 만들었다:
```java
public record BookSearchResult(List<BookResponse> content, long totalElements) {}
```
`Pageable`(페이지 번호/사이즈/정렬)은 캐시 "값"에 넣지 않고 메서드 파라미터(=캐시 키의 일부)로만 남긴다. `Page<BookResponse>` 재구성은 캐시된 `BookQueryService`가 아니라 `BookController`에서, 캐시 값(`BookSearchResult`)과 원래 요청의 `Pageable`을 합쳐서 한다:
```java
BookSearchResult result = bookService.search(new BookSearchCondition(keyword, category, status), pageable);
Page<BookResponse> books = new PageImpl<>(result.content(), pageable, result.totalElements());
```

## 3. Jackson 직렬화 함정과 해결

흔한 "Redis + Spring Cache + Jackson" 예제는 캐시 전역에 `GenericJackson2JsonRedisSerializer` + `objectMapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL)`을 쓴다. **이 레시피는 이 프로젝트에서 그대로 쓰면 깨진다.**

### 왜 깨지는지 (원리부터)

**Jackson의 "타입 힌트"가 왜 필요한가**:<br>
Redis에 저장된 JSON은 그냥 텍스트일 뿐이라, 나중에 꺼낼 때 Jackson은 "이 JSON을 어떤 자바 클래스로 되돌려야 하는지" 스스로 알 방법이 없다. `activateDefaultTyping`을 켜면, JSON을 저장할 때 그 값과 함께 `"@class": "com.dev24....BookResponse"` 같은 힌트를 같이 적어둔다. 나중에 꺼낼 때 이 힌트를 보고 "이건 `BookResponse`로 만들면 되겠구나" 하고 복원하는 것이다.

**`NON_FINAL`은 "언제 이 힌트가 필요한지"에 대한 정책**이다 — "final이 아닌 타입에 대해서만 이 힌트를 붙여라"는 뜻.<br>
이유는:

```java
class Animal {}
class Dog extends Animal {}

class Cage {
    Animal pet;  // 선언된 타입은 Animal
}
```
`pet` 필드에는 실제로 `Dog` 인스턴스가 들어있을 수도 있다(다형성). JSON만 보고는 "이게 `Animal`인지 `Dog`인지" 구분이 안 되니, 이런 경우엔 힌트(`@class`)가 꼭 필요하다.

반대로 `Animal`이 `final class Animal`이라면 얘기가 다르다.<br>
`final`은 "상속 불가"라는 뜻이니, `Animal` 타입 자리에 들어올 수 있는 건 오직 `Animal` 그 자체뿐이고 `Dog` 같은 서브클래스는 애초에 존재할 수 없다. 그러니 Jackson 입장에서는 "이건 힌트 안 붙여도 뻔히 `Animal`이니까 생략해도 된다"고 판단한다.<br>
이게 **"정확히 final 타입을 제외한다"**는 말의 의미다 — final 타입일 때만 딱 골라서 힌트를 안 붙이는 것.

**그런데 이게 왜 우리 경우엔 문제가 되나**:<br>
`BookResponse`는 `record`라서 자바 언어 규칙상 자동으로 `final`이다. `NON_FINAL` 정책 입장에서는 "final이니까 힌트 안 붙여도 됨" → Redis에는 `@class` 힌트 없이 그냥 순수 JSON만 저장된다(`{"id":1,"title":"자바의 정석", ...}`).<br>
문제는, Redis 캐시에서 값을 꺼낼 때 Jackson은 "이걸 `BookResponse`로 만들어야지"라는 정보를 코드 어디에서도 미리 받지 못한다는 것이다(`RedisCache`가 값을 그냥 `Object`로 다루기 때문 — 아래 `RedisSerializer<Object>` 참고). 그래서 힌트도 없고 목표 타입도 모르는 채로, "어쨌든 JSON 객체니까"라는 이유로 그냥 `LinkedHashMap`(자바 범용 맵)으로 만들어버린다. `BookResponse`가 아니라 `LinkedHashMap`이 튀어나오니, 코드에서 그걸 `BookResponse`로 쓰려는 순간 `ClassCastException`이 난다.

정리하면:
- `NON_FINAL` = "final이 아닌 타입에만 힌트를 붙인다"는 정책
- `record`는 자동으로 `final`
- 그래서 `BookResponse`(record)엔 힌트가 안 붙음
- 힌트가 없으니 꺼낼 때 Jackson이 원래 타입을 못 알아내고 `Map`으로 떨어짐
- → 캐시 히트할 때마다 `ClassCastException`

### 공식 문서/실제 코드 근거

- `BookResponse`/`BookSearchResult`는 **record**이고, 자바 record는 암묵적으로 `final`이다.
- `DefaultTyping.NON_FINAL`은 Jackson 공식 문서에 "typing information is needed for all non-final types"라고 되어 있다 — **정확히 final 타입을 제외**한다.
- `RedisCache`는 값을 `RedisSerializer<Object>`로 저장/조회한다(읽는 시점엔 정적 타입 정보가 없음).<br>
타입 힌트가 안 붙으면 역직렬화 결과가 `BookResponse`가 아니라 `LinkedHashMap`이 되어 **캐시 히트 시 `ClassCastException`** 이 난다.
- 이걸 우회하는 `DefaultTyping.EVERYTHING`은 Jackson 2.17부터 `@Deprecated`가 붙었다.

### 주의: `GenericJackson2JsonRedisSerializer`는 만드는 방법에 따라 결과가 다르다

앞의 설명("record는 final이라 힌트가 안 붙는다")은 **모든 경우에 적용되는 게 아니다.** `GenericJackson2JsonRedisSerializer`를 만드는 방법이 두 가지 있고, 방법에 따라 결과가 다르다. `spring-data-redis:3.5.13` 소스(`GenericJackson2JsonRedisSerializer.java`)를 직접 읽고 확인한 내용이다.

**방법 A — 내가 직접 만든 `ObjectMapper`를 넘기는 경우** (블로그/튜토리얼에서 흔히 보는 방식):
```java
ObjectMapper myMapper = new ObjectMapper();
myMapper.activateDefaultTyping(myMapper.getPolymorphicTypeValidator(), ObjectMapper.DefaultTyping.NON_FINAL);
var serializer = new GenericJackson2JsonRedisSerializer(myMapper);
```
소스를 보면, 이 생성자(`GenericJackson2JsonRedisSerializer(ObjectMapper mapper)`)는 내가 넘긴 `myMapper`를 **그대로** 쓴다<br>
— Spring Data Redis가 중간에 끼어들어서 타이핑 설정을 바꾸지 않는다.<br>
그러니 `myMapper`에 걸어둔 `NON_FINAL` 정책이 그대로 적용되고, 앞에서 설명한 대로 `BookResponse`(record=final)는 힌트가 안 붙어서 `ClassCastException`이 난다.

**방법 B — 인자 없는 생성자를 그냥 쓰는 경우**:
```java
var serializer = new GenericJackson2JsonRedisSerializer();
```
이 경우엔 내가 `ObjectMapper`를 안 만들어줬으니, `GenericJackson2JsonRedisSerializer`가 **자기 내부에서 새 `ObjectMapper`를 만들고 자기만의 타이핑 규칙**을 적용한다.<br>
그 규칙은 Jackson의 `NON_FINAL`이 아니라 Spring Data Redis가 직접 짠 코드다:
```java
// GenericJackson2JsonRedisSerializer.java 내부 (spring-data-redis 3.5.13)
public boolean useForType(JavaType javaType) {
    ...
    if (javaType.isFinal()
            && !KotlinDetector.isKotlinType(javaType.getRawClass())
            && javaType.getRawClass().getPackageName().startsWith("java")) {
        return false;   // 힌트 생략
    }
    return true;        // 그 외엔 final이어도 힌트 붙임
}
```
이 코드가 하는 말은:<br>
"final이더라도, 그 클래스 패키지가 `java.`로 시작하는 JDK 내장 타입(`java.lang.String` 같은 것들)일 때만 힌트를 생략하고, 그 외에는(우리가 만든 `BookResponse`처럼 `com.dev24...` 패키지인 것들은) final이어도 힌트를 붙인다"는 것이다.<br>
그래서 이 방법 B로 만들면, `BookResponse`가 final이어도 힌트가 정상적으로 붙어서 캐시 히트 시 문제가 안 생긴다.

**정리하면**:

| 어떻게 만들었나 | 타이핑 규칙 | `BookResponse`(record) 처리 |
|---|---|---|
| 방법 A: 직접 만든 `ObjectMapper`(NON_FINAL) + `GenericJackson2JsonRedisSerializer(myMapper)` | 진짜 Jackson `NON_FINAL` | 힌트 안 붙음 → 캐시 히트 시 `ClassCastException` |
| 방법 B: `new GenericJackson2JsonRedisSerializer()` (인자 없음) | Spring Data Redis 자체 규칙(`useForType`) | JDK 내장 타입만 예외, `BookResponse`는 힌트 붙음 → 정상 |

즉 "record라서 무조건 깨진다"가 아니라, **"내가 직접 만든 `ObjectMapper`에 `NON_FINAL`을 걸어서 넘겼을 때만" 깨진다.**<br>
그런데 인터넷에서 흔히 보는 튜토리얼은 대부분 방법 A(커스텀 `ObjectMapper` + `NON_FINAL`)를 소개하기 때문에, 그 레시피를 그대로 따라 하면 이 프로젝트에서는 깨지는 것이다.

**이 프로젝트는 이 A/B 구분 자체를 신경 쓸 필요가 없다** — 아예 다른 방법 C(아래 "해결" 참고)를 쓰기 때문이다:<br>
다형성 타입 힌트(`@class`)에 의존하는 대신, 캐시 이름마다 어떤 자바 타입인지(`BookResponse`인지 `BookSearchResult`인지) 미리 못박아두는 `Jackson2JsonRedisSerializer<T>`를 쓴다.<br>
그러면 애초에 "힌트가 붙었는지"를 따질 필요가 없다 — 꺼낼 때 이미 무슨 타입으로 만들지 알고 있으니까.

**해결**:<br>
이 프로젝트가 캐싱할 타입은 `BookResponse`/`BookSearchResult` 두 가지로 고정돼 있으므로, 다형성 타입 힌트 자체를 포기하고 캐시 이름별로 대상 타입을 미리 알고 있는 `Jackson2JsonRedisSerializer<T>(ObjectMapper, Class<T>)`를 쓴다<br>
(`spring-data-redis`가 제공하는 생성자, 타입 바인딩된 시리얼라이저라 `@class` 메타데이터 자체가 필요 없다 — 임의 클래스명을 JSON에서 읽어 역직렬화하지도 않으니 더 안전하기도 하다):

```java
// BookCacheConfig.java
private <T> RedisCacheConfiguration cacheConfig(ObjectMapper objectMapper, Class<T> type) {
    return RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(DEFAULT_TTL)
            .disableCachingNullValues()
            .serializeValuesWith(SerializationPair.fromSerializer(
                    new Jackson2JsonRedisSerializer<>(objectMapper, type)));
}
```
캐시 이름마다 이 설정을 등록한다(`RedisCacheManagerBuilderCustomizer` 빈 — Spring Boot가 자동으로 픽업하므로 `RedisCacheManager` 빈을 직접 만들 필요 없다):
```java
@Bean
public RedisCacheManagerBuilderCustomizer bookCacheCustomizer() {
    // GenericJackson2JsonRedisSerializer/Jackson2JsonRedisSerializer가 내부적으로 만드는 기본 ObjectMapper는 JavaTimeModule 명시적으로 등록 필요
    ObjectMapper redisObjectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    return builder -> builder
            .withCacheConfiguration(BOOK_SEARCH_CACHE, cacheConfig(redisObjectMapper, BookSearchResult.class))
            .withCacheConfiguration(BOOK_DETAIL_CACHE, cacheConfig(redisObjectMapper, BookResponse.class));
}
```
**주의**:<br>
새 `@Cacheable` 캐시를 추가하면 반드시 여기에 `withCacheConfiguration(...)`을 함께 추가해야 한다.<br>
등록되지 않은 캐시 이름은 Boot 기본값(JDK 직렬화)으로 떨어져 non-Serializable DTO에서 예외가 난다 — 등록을 빠뜨리면 조용히 성공하는 대신 시끄럽게(fail-fast) 실패하므로, 실수를 알아차리기는 오히려 쉽다.

**`LocalDate` 함정**:<br>
`GenericJackson2JsonRedisSerializer`/`Jackson2JsonRedisSerializer`가 내부적으로 만드는 기본 `ObjectMapper`는 Spring Boot 자동설정 `ObjectMapper`와 달리 `JavaTimeModule`을 자동 등록하지 않는다. `BookResponse.publishedAt`(`LocalDate`) 직렬화를 위해 위 코드처럼 명시적으로 등록해야 한다(`jackson-datatype-jsr310`은 `spring-boot-starter-web`을 통해 이미 클래스패스에 있으므로 `build.gradle.kts`에 의존성을 추가할 필요는 없다).

## 4. 캐시 키 생성

Redis 키는 결국 문자열이다 — `RedisCache`는 키 객체에 대해 컨버터가 없으면 `key.toString()`으로 변환한다.<br>
그래서 커스텀 `key=` SpEL 없이 기본 `SimpleKeyGenerator`를 써도 안전한지는 파라미터 타입들의 `toString()`이 **내용 기반**인지에 달려 있다:
- `BookSearchCondition`은 record라 `toString()`이 `BookSearchCondition[keyword=..., category=..., status=...]` 형태로 내용 기반이다.
- `Pageable`/`PageRequest`/`Sort`도 Spring Data 자체에서 내용 기반 `toString()`을 구현한다(`"Page request [number: 0, size 20, sort: id: ASC]"` 형태).

두 조건이 만족되므로 `search(condition, pageable)`은 커스텀 키 없이 `@Cacheable(cacheNames = "bookSearch")`만으로 충분하다. `getDetail(Long id)`는 파라미터가 `Long` 하나뿐이라 더 단순하다.

**주의**:<br>
이건 "지금 쓰는 파라미터 타입들이 우연히 다 내용 기반 `toString()`을 가져서" 안전한 것이지, `SimpleKeyGenerator`가 항상 안전하다는 뜻은 아니다. 나중에 `toString()`을 오버라이드하지 않은 일반 클래스를 캐시 대상 메서드의 파라미터로 추가하면, `Object.toString()`(식별자/해시코드 기반)으로 떨어져 호출마다 다른 키가 생성되어 캐시가 조용히 항상 미스로 돈다(에러는 안 나고 그냥 캐시 효과가 없어짐) — 그런 타입을 추가할 땐 커스텀 `key=` SpEL을 고려해야 한다.

## 5. 상세 조회 엔드포인트

이번 작업 전에는 `GET /api/books/{id}` 자체가 없었다. 새로 추가하며 QueryDSL이 아니라 평범한 JPQL `@Query`를 택했다:
```java
// BookRepository.java
@Query("select b from Book b left join fetch b.bookImage left join fetch b.rating where b.id = :id")
Optional<Book> findByIdWithDetails(@Param("id") Long id);
```
`BookQueryRepository`(QueryDSL)는 **동적** 검색(선택적 keyword/category/status 조건 조합)을 위한 추상화다. 상세 조회는 조건이 전혀 안 바뀌는 고정 쿼리(id로 1건 + fetch join)라 QueryDSL을 쓸 이유가 없다 — `existsByIsbn`처럼 `BookRepository`에 바로 추가했다.

`bookImage`/`rating`을 fetch join하는 이유는 검색 쿼리(`BookQueryRepositoryImpl.search()`)와 동일하다:<br>
`application.properties`의 `spring.jpa.open-in-view=false` 때문에, fetch join 없이 지연 로딩 필드(`book.getBookImage()` 등)를 서비스/컨트롤러 시점에 건드리면 트랜잭션(세션)이 이미 닫혀 있어 `LazyInitializationException`이 난다.

존재하지 않는 id는 `Optional`/`null`을 반환하는 대신 예외를 던진다:
```java
@Cacheable(cacheNames = BookCacheConfig.BOOK_DETAIL_CACHE)
public BookResponse getDetail(Long id) {
    Book book = bookRepository.findByIdWithDetails(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
    return BookResponse.from(book);
}
```
`ErrorCode.ENTITY_NOT_FOUND`(404, `C004`)는 이미 정의돼 있었지만 이전까지 아무 데서도 쓰이지 않던 코드였다 — 이번에 처음 실사용됐다.<br>
**`@Cacheable`은 메서드가 정상적으로 값을 반환할 때만 캐시에 쓰고, 예외가 전파되면 캐시에 아무것도 남기지 않는다** — 그래서 "이 id는 없다"는 사실 자체가 캐시에 남아 나중에 그 id로 진짜 책이 생겨도 계속 404가 나오는 문제는 생기지 않는다.

## 6. 테스트 전략

`BookQueryServiceCacheTest`(`src/test/java/com/dev24/bookstore/book/service/`)는 "캐시가 있다"를 Mockito로 "메서드가 1번만 호출됐다"는 식으로 검증하지 않는다. 대신 이 프로젝트가 이전 작업(N+1 방지, `BookQueryRepositoryNPlusOneTest`)에서 쓴 것과 같은 방식 — **Hibernate `Statistics`로 실제 쿼리 실행 횟수를 실측**한다:

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@Testcontainers
class BookQueryServiceCacheTest {

    @Container @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Test
    void getDetail_secondCall_isServedFromRedisWithoutHittingDatabase() {
        // ... 도서/이미지/평점 저장 ...
        statistics.clear();

        BookResponse first = bookQueryService.getDetail(id);
        long queriesAfterFirstCall = statistics.getQueryExecutionCount();
        assertThat(queriesAfterFirstCall).isGreaterThan(0);   // 캐시 미스 - 실제 SQL이 나갔어야 함

        BookResponse second = bookQueryService.getDetail(id);
        assertThat(statistics.getQueryExecutionCount()).isEqualTo(queriesAfterFirstCall); // 캐시 히트 - 추가 쿼리 0건
        assertThat(second).isEqualTo(first);   // Redis 왕복 후에도 값(LocalDate 포함)이 그대로 보존됨
    }
}
```

핵심 포인트:
- **Redis는 진짜 Testcontainers 컨테이너**를 쓴다(`GenericContainer("redis:7-alpine")` + `@DynamicPropertySource`).<br>
Postgres처럼 `@ServiceConnection`을 바로 못 쓰는 이유는, 이 프로젝트에 Redis 전용 Testcontainers 모듈 의존성이 없고(`build.gradle.kts`엔 `testcontainers:postgresql`만 있음), 인증 모듈의 `AuthIntegrationTest`가 이미 써온 `GenericContainer` + `@DynamicPropertySource` 패턴을 그대로 재사용한 것이다.
- `@SpringBootTest(webEnvironment = NONE)`을 쓴 이유:<br>
  - `@Cacheable`의 AOP 프록시가 실제로 동작하려면 `@Service` 빈이 정말로 스프링 컨테이너에 올라가야 한다 — `@DataJpaTest`(JPA 계층만 올리는 슬라이스 테스트)로는 안 되고, 웹 서버까지는 필요 없으니 `WebEnvironment.NONE`으로 최소화했다.
- `LocalDate` 값을 넣고 `second.equals(first)`까지 확인한 이유:<br>
  - 단순히 "캐시가 동작한다"만이 아니라, **3절의 `JavaTimeModule` 등록이 실제로 Redis 왕복(직렬화→역직렬화)에서 작동하는지**까지 증명하기 위해서다.

## 7. 캐시 히트/미스 응답시간 실측

`docs/PERFORMANCE.md`의 "Redis 캐시 히트/미스 응답시간 비교" 절에 원문 curl 결과가 있다. 요약:

| 엔드포인트 | 미스 평균 | 히트 평균 | 개선 |
|---|---|---|---|
| `GET /api/books/{id}` (상세) | 11.5 ms | 6.4 ms | ~1.8배 |
| `GET /api/books?category=...` (목록) | 12.4 ms | 2.3 ms | ~5.4배 |

측정 시 중요했던 두 가지 방법론(자세한 내용/원문 수치는 `docs/PERFORMANCE.md` 참고):
1. **먼저 몸풀기(워밍업) 없이는 측정하지 않았다**:<br>
서버를 막 켰을 때는 JVM이 코드를 아직 최적화하지 못했고 DB 커넥션 풀도 비어 있어서, 첫 요청들은 원래보다 느리게 나온다. 이 상태에서 바로 "캐시 미스" 시간을 재면 캐시 효과가 아니라 "서버가 막 켜진 효과"까지 같이 섞여버린다. 그래서 측정 전에 캐시와 상관없는 요청을 여러 번 미리 보내 서버를 데운 뒤에 측정했다.
2. **미스와 히트를 번갈아가 아니라 각각 몰아서 측정했다**:<br>
처음에는 "미스 1번 → 히트 1번 → 미스 1번 → 히트 1번..." 식으로 번갈아 쟀다. 그런데 로컬 환경에서는 응답 시간이 원래도 몇 밀리초(ms)밖에 안 돼서, OS가 다른 작업을 처리하느라 잠깐 느려지는 것 같은 잡음(노이즈)에도 값이 쉽게 들쭉날쭉했다. 그래서 "서로 다른 책 10권을 조회해 미스만 10번 모으기", "같은 책을 10번 조회해 히트만 10번 모으기" 방식으로 바꿔 각각의 평균을 비교했더니 훨씬 안정적인 결과가 나왔다.

목록 검색이 상세 조회보다 캐시 효과가 큰 이유(5.4배 vs 1.8배):<br>
상세 조회는 이미 PK로 단일 행만 가져오는 가벼운 쿼리라 DB 조회 자체가 원래도 빨랐던 반면, 목록 검색은 QueryDSL 동적 조건 + 두 개의 fetch join을 매번 다시 계산해야 해서 DB 왕복 비용이 상대적으로 크고, 캐시로 건너뛸 때 이득도 그만큼 크다.

## 8. 직접 실행해보기

```bash
# 1. Postgres + Redis 기동
docker compose up -d postgres redis

# 2. 앱 로컬 실행 (.env의 실제 카카오 API 키로 실제 도서 시딩)
export APP_JWT_SECRET=...          # .env 참고
export APP_KAKAO_REST_API_KEY=...  # .env 참고
export APP_BOOK_SEED_ENABLED=true
./gradlew bootRun

# 3. 목록 조회 - 첫 호출(미스) vs 재호출(히트) 응답시간 비교
curl -s -o /dev/null -w "%{time_total}\n" "http://localhost:8080/api/books?category=소설&page=0&size=20"
curl -s -o /dev/null -w "%{time_total}\n" "http://localhost:8080/api/books?category=소설&page=0&size=20"

# 4. 상세 조회도 동일하게 (임의의 실제 도서 id로)
curl -s -o /dev/null -w "%{time_total}\n" "http://localhost:8080/api/books/1"
curl -s -o /dev/null -w "%{time_total}\n" "http://localhost:8080/api/books/1"

# 5. Redis에 실제로 키가 생겼는지 확인
docker compose exec redis redis-cli KEYS "*"
docker compose exec redis redis-cli GET "bookDetail::1"   # JSON으로 저장된 BookResponse 확인
```

## 9. 범위 밖으로 남긴 것

- **`@CacheEvict`**: MODERNIZATION_PLAN.md Phase 3의 다음 체크리스트 항목. 현재 도서 등록/수정 엔드포인트 자체가 없어(`BookSeedService`는 최초 1회 시딩 배치일 뿐, 실시간 쓰기 경로가 아님) 지금은 무효화할 쓰기 경로가 없다. 쓰기 엔드포인트가 생기면 `@CacheEvict(cacheNames = {"bookSearch", "bookDetail"}, ...)`를 그 메서드에 붙이면 된다.
