# Docker Compose 인프라 가이드

`docker compose up` 한 번으로 앱(2개 복제) + Postgres(Flyway) + Redis + NATS JetStream + Nginx(리버스 프록시/로드밸런서)를 로컬에 띄우는 구성이다.

## 한눈에 보는 전체 구조

```
                     사용자 요청 (localhost:8080)
                              │
                        ┌─────▼─────┐
                        │   nginx   │  리버스 프록시 + 로드밸런서
                        └─────┬─────┘
                 ┌────────────┴────────────┐
                 ▼                         ▼
           ┌───────────┐             ┌───────────┐
           │   app-1   │             │   app-2   │   같은 이미지, 2개 컨테이너
           └─────┬─────┘             └────┬──────┘
                 └───────────┬────────────┘
        ┌────────────────────┼─────────────────────┐
        ▼                    ▼                     ▼
  ┌───────────┐        ┌───────────┐         ┌───────────┐
  │ postgres  │        │   redis   │         │   nats    │
  │ (Flyway)  │        │           │         │(JetStream)│
  └───────────┘        └───────────┘         └───────────┘
```

| 서비스 | 역할 | 호스트 포트 | mem_limit |
|---|---|---|---|
| nginx | 리버스 프록시 / 로드밸런서 | 8080 | 64m |
| app (×2) | Spring Boot 애플리케이션 | (직접 노출 안 함, nginx로만 접근) | 512m |
| postgres | 메인 DB (Flyway로 스키마 관리) | 5432 | 512m |
| redis | 캐시 / 세션 저장용 | 6379 | 256m |
| nats | 비동기 이벤트 브로커(JetStream) | 4222, 8222 | 128m |

## 목차

1. 사전 준비
2. 왜 멀티스테이지 Dockerfile인가
3. 왜 healthcheck + depends_on이 필요한가
4. 왜 서비스별 mem_limit을 두는가
5. Nginx가 두 app 인스턴스에 분산되는 원리
6. 왜 nginx 이미지 버전을 고정했는가 — CVE-2026-42945
7. 왜 Flyway 베이스라인 마이그레이션이 필요한가
8. 직접 실행하기
9. 문제 해결

---

## 1. 사전 준비

- **Docker Desktop**을 설치하고 실행해둔다. `docker info`를 실행했을 때 에러 없이 정보가 나오면 데몬이 정상적으로 떠 있는 것이다.
- **Compose 버전 확인**: `docker compose version`을 실행해본다 (하이픈 없이 `docker compose`). 결과가 나와야 정상이다.
  - 만약 이 명령이 없고 하이픈이 들어간 구버전 `docker-compose`만 있다면, 6번 섹션에서 쓰는 `deploy.replicas` 설정이 조용히 무시되니 Docker Desktop을 최신으로 올려야 한다.
- **포트 확인**: 아래 포트가 로컬에서 비어 있어야 한다. 이미 다른 프로그램이 쓰고 있으면 충돌한다.
  - `5432` (Postgres), `6379` (Redis), `4222`/`8222` (NATS), `8080` (Nginx)

---

## 2. 왜 멀티스테이지 Dockerfile인가

> **핵심 한 줄**: 빌드에 필요한 도구(JDK, Gradle)는 이미지를 만들 때만 쓰고, 실제로 앱을 돌릴 때는 JRE(실행 전용)만 남긴다 — 이미지를 가볍고 안전하게 만들기 위해서다.

`Dockerfile`은 두 단계(스테이지)로 나뉜다.

```
# 빌드 전용 (JDK + Gradle)
FROM eclipse-temurin:21-jdk-alpine AS builder
... (중간 생략)
# 실행 전용 (JRE만)
FROM eclipse-temurin:21-jre-alpine AS runtime
```

**왜 두 단계로 나누나?**<br>
컴파일하려면 JDK(컴파일러 포함)와 Gradle 캐시가 필요하지만, 컴파일이 끝난 jar 파일을 실행할 때는 JRE(실행기)만 있으면 된다. 한 단계로 몰아넣으면 최종 이미지에 컴파일러·소스코드·Gradle 캐시까지 그대로 남아서 이미지가 불필요하게 커지고, 공격 표면(쓸모없이 딸려온 도구들)도 늘어난다.

**빌드 캐싱 순서가 중요한 이유**<br>
빌더 스테이지에서 `gradlew`/`build.gradle.kts`/`gradle/`를 먼저 복사해 `./gradlew dependencies`로 의존성만 먼저 받아두고, 그다음에야 `src`를 복사한다. Docker는 각 명령 결과를 레이어로 캐싱하는데, 소스 코드만 바뀌고 의존성 목록이 안 바뀌었다면 의존성 다운로드 레이어가 캐시에서 그대로 재사용되어 재빌드가 훨씬 빨라진다.

**jar 파일 이름을 고정한 이유**<br>
런타임 스테이지는 `COPY --from=builder`로 결과물 jar 하나만 가져온다. `build.gradle.kts`에 `bootJar { archiveFileName.set("app.jar") }`을 지정해뒀는데, 지정하지 않으면 jar 이름이 `bookstore-0.0.1-SNAPSHOT.jar`처럼 버전 문자열을 포함하게 되어, 버전을 올릴 때마다 Dockerfile의 `COPY` 경로도 같이 고쳐야 하기 때문이다.

**`RUN chmod +x ./gradlew`가 있는 이유**<br>
이 저장소는 Windows에서 체크아웃되는 경우가 있어 실행 비트가 보존되지 않을 수 있다. 리눅스 빌드 컨테이너 안에서 실행 권한을 명시적으로 부여해야 `./gradlew`가 "Permission denied"로 실패하지 않는다.

**non-root 사용자(`spring`)로 실행하는 이유**<br>
컨테이너가 뚫려도 root 권한을 바로 주지 않기 위한 최소한의 방어다. 실제로 이렇게 되어 있는지는 런타임 스테이지 코드를 보면 알 수 있다.

```dockerfile
FROM eclipse-temurin:21-jre-alpine AS runtime
RUN addgroup -S spring && adduser -S spring -G spring   # spring이라는 계정/그룹 생성
WORKDIR /app
COPY --from=builder /app/build/libs/app.jar ./app.jar
RUN chown spring:spring /app/app.jar                     # jar 소유자를 spring으로 변경
USER spring                                              # 이후 명령/실행을 spring 계정으로 전환
```

핵심은 `USER spring` 줄이다.<br>
Dockerfile에서 `USER` 지시어는 그 시점부터 이후 실행되는 모든 것(여기서는 마지막 `ENTRYPOINT`로 뜨는 `java -jar` 프로세스)을 지정한 사용자 권한으로 돌리라는 뜻이다. `USER` 지시어가 없으면 컨테이너는 기본값인 root로 실행된다. `docker exec <container_id> whoami`로 확인해보면 `root`가 아니라 `spring`이 나온다.

**`.dockerignore`파일**<br>
`.git`, `build`, `legacy`, `docs` 등을 제외해 이미지 빌드 시 전송되는 파일 양(빌드 컨텍스트)을 줄인다.

---

## 3. 왜 healthcheck + `depends_on` 조건이 필요한가

> **핵심 한 줄**: "컨테이너가 켜졌다"와 "서비스가 요청을 받을 준비가 됐다"는 다르다. 준비될 때까지 기다리게 해야 앱이 DB보다 먼저 뜨는 레이스 컨디션을 막을 수 있다.

단순히 `depends_on: [postgres]`만 쓰면 Compose는 **postgres 컨테이너 프로세스가 시작되었는지만** 확인하고 바로 app을 띄운다.<br>
하지만 Postgres 프로세스가 뜬 것과 "연결을 받을 준비가 됐다"는 것은 다르다 — 실제로는 내부 초기화 스크립트 실행 등으로 수 초의 텀이 있다. 이 텀 사이에 app이 뜨면 Flyway가 접속을 시도하다 실패하고, `docker compose up`이 어떤 날은 되고 어떤 날은 안 되는 재현하기 어려운 버그가 된다.

그래서 각 서비스에 **healthcheck**(컨테이너가 정상 동작 중인지 주기적으로 확인하는 기능)를 정의하고,

```yaml
depends_on:
  postgres:
    condition: service_healthy
```

로 "healthcheck를 통과할 때까지" 기다리게 했다. 이번 구성에서 쓴 healthcheck들:

| 서비스 | 확인 명령 | 의미 |
|---|---|---|
| postgres | `pg_isready -U bookstore -d bookstore` | 지정 DB로 연결을 받을 준비가 됐는지 |
| redis | `redis-cli ping` | PONG 응답이 오는지 |
| nats | `wget -qO- http://localhost:8222/healthz` | NATS 모니터링 포트(`-m 8222`)의 헬스 엔드포인트 |
| app | `wget --spider -q http://localhost:8080/actuator/health` | Spring Boot Actuator 헬스 엔드포인트 |
| nginx | `wget --spider -q http://127.0.0.1/swagger-ui/index.html` | nginx 프로세스 자체뿐 아니라 nginx→app 프록시 체인이 실제로 2xx를 반환하는지까지 같이 검증한다 |

**nginx만 처음엔 healthcheck가 없었던 이유와 나중에 추가한 이유**: nginx 뒤에는 아무도 의존하는 서비스가 없어서(체인의 맨 끝) 원래는 굳이 안 넣었는데, 그러면 `docker compose ps`로 전체 상태를 볼 때 nginx만 항상 `Started`로만 남아서 실제로 요청을 정상 처리하고 있는지 알 수 없었다. 관찰 가능성을 다른 서비스와 맞추려고 추가했다.

**추가하면서 실제로 겪은 함정 두 가지**:
- 처음엔 `wget --spider -q http://localhost:80/`으로 썼는데 `Connection refused`로 계속 실패했다. 원인은 컨테이너 안에서 `localhost`가 IPv6(`::1`)로 먼저 풀리는데, `default.conf`의 `listen 80;`은 IPv4만 리스닝하기 때문이었다. `localhost` 대신 `127.0.0.1`로 바꿔서 해결했다.
- `127.0.0.1`로 바꿔도 여전히 실패했는데, 이번엔 대상 경로가 `/`(루트)였기 때문이다. Spring Boot 쪽에 루트 경로 매핑이 없어서 nginx가 정상적으로 프록시했는데도 app이 404를 반환했고, `wget --spider`는 2xx/3xx가 아니면 실패로 처리한다. 확실히 200을 반환하는 `/swagger-ui/index.html`로 바꿔서 해결했다.

`app`의 healthcheck에는 아래 네 옵션을 함께 줬다. 각각의 의미:

- `interval: 10s` — 헬스체크 명령을 10초 간격으로 계속 실행한다 (start_period 동안에도 동일하게 적용됨).
- `timeout: 5s` — 한 번의 헬스체크 명령이 5초 안에 응답하지 않으면 그 시도는 실패로 간주한다.
- `retries: 10` — 연속으로 10번 실패해야 최종적으로 "unhealthy" 상태로 확정한다.
- `start_period: 30s` — 컨테이너가 뜬 직후 30초 동안 발생하는 실패는 `retries` 카운트에 넣지 않는다. 이 기간에도 10초마다 체크는 똑같이 돌지만 실패해도 무시되고, 30초 안에 한 번이라도 성공하면 그 즉시 이 유예 기간이 끝나고 정상적인 healthy/unhealthy 판정으로 넘어간다.

`start_period`를 둔 이유는, JVM 기동 + Flyway 마이그레이션 + Hibernate 메타데이터 검증까지 끝나는 데 시간이 걸리기 때문이다. 이 초기 부팅 구간의 "당연한 실패"까지 `retries`에 넣으면, 컨테이너가 정상적으로 뜨고 있는 중인데도 조기에 unhealthy로 판정될 수 있다.

`nginx`도 `app: condition: service_healthy`에 의존한다 — app이 완전히 뜨기 전에 nginx가 트래픽을 넘기면 502가 뜨기 때문이다.

---

## 4. 왜 서비스별 `mem_limit`을 두는가

> **핵심 한 줄**: 실제 운영 환경처럼 메모리 상한을 걸어두면, 설정 실수나 메모리 누수를 로컬에서 먼저 잡을 수 있다.

| 서비스 | mem_limit |
|---|---|
| postgres | 512m |
| redis | 256m |
| nats | 128m |
| app | 512m |
| nginx | 64m |

**JVM에서 주의할 점**:<br>
Java 21은 컨테이너의 메모리 제한(cgroup)을 자동으로 인식해서, 기본적으로 힙을 컨테이너 `mem_limit`의 **약 25%**로 잡는다(`-XX:MaxRAMPercentage=25.0`이 기본값).

`app`에 `mem_limit: 512m`만 주면 힙이 약 128MB로 제한되는데, Spring Boot 3.5 + Hibernate + Actuator를 함께 띄우기엔 빠듯하다. 그래서 `docker-compose.yml`의 `app` 서비스에 다음을 추가해 힙 비율을 75%까지 늘렸다.

```yaml
environment:
  JAVA_TOOL_OPTIONS: -XX:MaxRAMPercentage=75.0
```

이러면 컨테이너 전체 512MB 중 최대 384MB 정도까지 힙으로 쓸 수 있다. 이 옵션이 없으면 트래픽이 몰릴 때 힙 부족으로 GC 압박이 심해지거나 OOM(메모리 부족으로 프로세스가 강제 종료됨)이 날 수 있다.

`docker stats`로 실제 사용량이 제한 이내인지 확인할 수 있다 (8번 섹션 참고).

---

## 5. Nginx가 두 app 인스턴스에 분산되는 원리 (직접 겪은 함정 포함)

> **핵심 한 줄**: 정적 upstream 설정은 실제로 두 컨테이너에 분산되지 않았다. nginx 1.27.3부터 오픈소스에도 풀린 `resolve` 파라미터를 써서, 컨테이너가 뜨고 죽을 때마다 upstream 목록이 자동으로 갱신되게 만들었다 — 재시작 없이 3개로 늘려도 즉시 인식하는 걸 직접 확인했다.

`docker-compose.yml`의 `app` 서비스는 `deploy: replicas: 2`로 되어 있어 `dev24-app-1`, `dev24-app-2` 두 컨테이너가 뜬다. `deploy.replicas`는 Swarm 모드가 아니어도 최신 Compose V2(`docker compose` CLI)에서 그대로 동작한다 — 단, `app` 서비스에는 고정된 `container_name`도, 호스트 포트 매핑도 주면 안 된다. 둘 다 컨테이너가 여러 개일 때 충돌하기 때문이다.

### 처음에 시도했다가 실패한 방법

nginx 설정에 아래처럼 정적 upstream을 쓰면 라운드로빈(요청을 번갈아 분산하는 방식)이 될 거라 예상했다.

```nginx
upstream bookstore_app {
    server app:8080;
}
```

nginx 공식 문서에는 "도메인 이름이 여러 IP로 resolve되면 그 자체로 여러 개의 서버를 정의한 것"이라고 되어 있어서, 오픈소스 nginx도 이론적으로는 이렇게만 써도 여러 IP를 다 upstream에 등록해야 한다. 그런데 실제로 테스트해보니 (Docker Compose + Alpine nginx 조합에서) 재시작을 해봐도 매번 같은 컨테이너 하나로만 요청이 갔다. 즉 문서상 보장된 동작인데 우리 조합에서는 재현되지 않았다 — 정확한 원인(Alpine의 musl libc `getaddrinfo` 동작 차이인지, Docker 내장 DNS가 이 경로에서 응답하는 방식 때문인지)까지는 밝히지 못했다. 확실한 건 딱 하나, **정적 upstream은 이 환경에서 못 믿을 방법이라는 것**이었다.

더 근본적인 문제는, 이 방식이 근본적으로 "IP가 거의 안 바뀌는 전통적인 DNS 환경"을 가정하고 만들어졌다는 점이다.<br>
예를 들어 회사 도메인이 로드밸런서 뒤의 고정 IP 여러 개를 가리키는 경우엔 nginx가 시작할 때 한 번 resolve해서 그 IP들을 계속 써도 문제가 없다 — IP가 바뀔 일이 거의 없으니까. 반면 Docker Compose의 `app` 컨테이너는 재시작되거나 스케일이 바뀔 때마다 IP가 바뀐다. "설정 로드 시점에 딱 한 번만 resolve하고 그 뒤로는 다시 안 본다"는 방식 자체가, 컨테이너처럼 IP가 계속 바뀌는 환경과는 애초에 안 맞는 전제다.

### 실제로 동작하게 만든 방법: `resolve` 파라미터

nginx **1.27.3**(2024-11)부터 `upstream` 블록의 `server` 지시어에 `resolve` 파라미터가 **오픈소스에도 공개됐다** (그 전엔 NGINX Plus 전용 기능이었다)<br>
— `resolver`가 설정한 DNS로 주기적으로(TTL마다) 다시 조회해서, 컨테이너가 뜨고 죽을 때마다 upstream 목록을 자동으로 갱신한다. 우리가 쓰는 `nginx:1.30-alpine`은 이 버전보다 최신이라 바로 쓸 수 있다.

```nginx
upstream bookstore_app {
    zone bookstore_app 64k;
    resolver 127.0.0.11 valid=10s ipv6=off;
    server app:8080 resolve;
}
```

- `resolve` 파라미터는 서버 그룹이 **공유 메모리(shared memory)에 있어야 한다**는 제약이 있어서, `zone bookstore_app 64k;`로 이름 붙인 공유 메모리 영역을 함께 선언해야 한다. 이게 없으면 `resolving names at run time requires upstream ... to be in shared memory`라는 에러로 nginx가 기동에 실패한다 (실제로 처음 시도했을 때 이 에러로 막혔다).
- `resolver 127.0.0.11 valid=10s;`는 Docker의 내장 DNS를 10초 TTL로 사용하겠다는 뜻이다.
- `server app:8080 resolve;`의 `resolve`가 "이 서버 이름을 resolver로 주기적으로 다시 조회해라"는 지시다.

**실제로 검증한 결과**:<br>
이 설정으로 컨테이너 2개에 요청이 정확히 번갈아(`172.18.0.6`, `172.18.0.5`, `172.18.0.6`, `172.18.0.5`...) 분산되는 걸 확인했다. 더 나아가 nginx를 재시작하지 않고 `docker compose up -d --scale app=3`으로 컨테이너를 3개로 늘려봤더니, 몇십 초 안에 세 번째 컨테이너(`172.18.0.8`)도 자동으로 라운드로빈 대상에 포함됐다 — 이게 바로 `resolve` 파라미터가 하는 일이다.

### 왜 전통적인 "고정 IP + 로드밸런서" 방식은 못 쓰나 (프로젝트 범위와의 관계)

회사 도메인이 로드밸런서 뒤의 IP 몇 개를 가리키는, 거의 안 바뀌는 전통적인 구성과 비교해보면 이해가 더 쉽다.

- **전통적 환경이 안정적인 이유**:<br>
로드밸런서(ALB, F5 등) **자체의 IP만** DNS에 노출된다. 그 뒤의 실제 백엔드 서버가 늘어나거나 죽어도, DNS가 가리키는 대상(로드밸런서 IP) 자체는 안 바뀐다. nginx나 클라이언트는 안정적인 그 IP 하나만 알면 되고, 실제 분산은 로드밸런서가 알아서 한다.
- **우리 환경은 그 중간 계층이 없다**:<br>
이 프로젝트에서 nginx는 로드밸런서 앞단이 아니라 **nginx 자신이 로드밸런서**다. 그래서 nginx가 직접 진짜 app 컨테이너의 IP를 알아야 하는데, 이 IP는 컨테이너가 재시작되거나 스케일이 바뀔 때마다 바뀐다. "안정적인 가상 IP(VIP)가 중간에서 완충해주는" 계층 자체가 없는 것이다.
- **VIP를 쓸 방법이 없는 건 아니다**:<br>
Docker Swarm 모드로 전환하면 서비스마다 라우팅 메시(ingress network)가 고정된 Virtual IP를 주고, 실제 컨테이너 분산은 네트워크 레벨에서 투명하게 처리된다. Kubernetes의 `Service`(ClusterIP)도 같은 개념이다. 둘 다 "고정 IP + 뒤에서 알아서 분산"이라는 전통적인 패턴을 정확히 재현해준다.
- **다만 그건 이 프로젝트 범위 밖이다**:<br>
`MODERNIZATION_PLAN.md`가 잡은 인프라는 순수 Docker Compose(`docker compose up` 한 번으로 로컬에 뜨는 구성)이고, Swarm이나 Kubernetes 도입은 계획에 없다.<br>
그래서 이 프로젝트 안에서 실제로 쓸 수 있는 계층은 DNS뿐이고, 그 DNS는 컨테이너 IP 변화를 그대로 반영한다. 결과적으로 "한 번만 보고 끝"이 아니라 "짧은 TTL로 자주 다시 보는" `resolve` 방식이, 이 프로젝트 범위 안에서 나올 수 있는 가장 적합한 선택이었다.

### actuator 이중 차단

`/actuator/` 경로는 nginx에서 `deny all; return 404;`로 아예 막아뒀다. Spring 쪽에서도 `management.endpoints.web.exposure.include=health`로 `health`만 열어뒀지만, 그마저도 외부(nginx 앞단)에서는 노출하지 않는 이중 방어다. 컨테이너 자체 헬스체크는 nginx를 거치지 않고 컨테이너 내부에서 `localhost:8080`으로 직접 확인하므로 이 차단과 무관하게 동작한다.

---

## 6. 왜 `nginx` 이미지 버전을 고정했는가 — CVE-2026-42945

> **핵심 한 줄**: 처음 쓰던 `nginx:1.27-alpine`이 실제로 존재하는 심각한 취약점(CVSS 9.2)의 영향 버전에 포함되어 있어서, 패치된 `nginx:1.30-alpine`으로 올렸다.

개발 중 `ngx_http_rewrite_module`의 힙 버퍼 오버플로우 취약점 **CVE-2026-42945**("NGINX Rift")를 알게 됐다. nvd.nist.gov 등에서 확인한 내용:

- **CVSS 9.2** — 인증 없이 조작된 HTTP 요청만으로 트리거 가능. DoS는 물론, 조건이 맞으면 워커 프로세스 안에서 원격 코드 실행(RCE)까지 가능하다.
- **영향 버전: NGINX OSS 0.6.27 ~ 1.30.0** (Plus R32~R36 포함) — **1.30.1 / 1.31.0에서 패치됨**.
- **트리거 조건**: `rewrite` 지시어가 이름 없는 캡처(`$1`, `$2` 등)를 쓰고, 치환 문자열에 `?`가 들어가고, 같은 스코프에 또 다른 `rewrite`/`if`/`set`이 뒤따르는 설정 조합.

`docker/nginx/default.conf`엔 `rewrite` 지시어 자체가 없다(`set $upstream_app app;`은 있지만 캡처 그룹이 없고 앞뒤로 `rewrite`가 없음). 그래서 지금 설정으로는 이 트리거 조건에 해당하지 않는다. 그래도 `ngx_http_rewrite_module`은 기본으로 컴파일되어 있는 모듈이라, 취약한 버전의 바이너리를 그대로 쓰는 것 자체가 위험하다고 판단했다.

그래서 `docker-compose.yml`의 `nginx` 이미지를 패치가 포함된 stable 라인으로 올렸다.

```yaml
nginx:
  image: nginx:1.30-alpine   # 1.30.1+ 포함 (CVE-2026-42945 패치)
```

`docker compose exec nginx nginx -v`로 실제 받아온 버전이 `nginx/1.30.3`(패치 임계값 1.30.1 이상)인 걸 확인했다.

**앞으로 유지보수할 때 주의할 점**:<br>
`1.27-alpine`처럼 이미 지난 마이너 라인에 고정해두면 그 라인이 더 이상 패치를 받지 않을 수 있다. 주기적으로 nginx.org의 보안 공지(nginx.org/en/security_advisories.html)나 `docker scout cves nginx:1.30-alpine` 같은 스캐너로, 지금 쓰는 태그가 여전히 관리되는 stable 라인인지 확인하는 게 좋다.

---

## 7. 왜 Flyway 베이스라인 마이그레이션이 필요한가

> **핵심 한 줄**: 지금은 JPA 엔티티가 하나도 없어서 진짜 스키마가 없다. Flyway 이력 관리를 지금부터 시작해두려고, 아무 일도 안 하는 마이그레이션 파일 하나를 만들어뒀다.

`application.properties`에 `spring.jpa.hibernate.ddl-auto=validate`를 설정했다 — Hibernate가 스키마를 마음대로 만들거나 바꾸지 않고, Flyway가 관리하는 스키마와 엔티티가 일치하는지 **검증만** 하게 하려는 것이다 (스키마 소유권을 Flyway 하나로 통일).

문제는 현재(Phase 1) 시점엔 JPA 엔티티가 하나도 없다는 것이다. Flyway가 관리할 마이그레이션이 하나도 없으면 `flyway_schema_history`(Flyway가 적용 이력을 기록하는 테이블) 자체가 생기지 않는다. 그래서 최소한의 베이스라인 마이그레이션을 하나 만들어뒀다.

`src/main/resources/db/migration/V1__init.sql`:
```sql
SELECT 1;
```

이렇게 하면 첫 `docker compose up` 때부터 Flyway 이력 관리가 시작되고, Phase 2부터는 `V2__create_customer_table.sql`처럼 이어서 추가하면 된다.

(참고: `baseline-on-migrate` 옵션은 "이미 존재하는 수작업 스키마"를 나중에 Flyway 관리로 편입할 때 쓰는 기능이라, 지금처럼 빈 DB에서 시작하는 상황에는 맞지 않는다.)

---

## 8. 직접 실행하기 (단계별 명령어)

아래 명령어는 두 가지 버전으로 적었다.<br>
**Git Bash**에서 실행하면 bash 버전을, **Windows PowerShell**에서 실행하면 PowerShell 버전을 쓰면 된다.<br>

PowerShell에서 bash 버전을 그대로 쓰면 최소 두 가지가 막힌다: `curl`이 `Invoke-WebRequest` 별칭이라 `-i` 같은 curl 전용 옵션을 못 알아듣고, `head`/`grep` 같은 유닉스 명령이 없다.

```bash
# 1. 이미지 빌드
docker compose build

# 2. 전체 스택 기동 (백그라운드)
docker compose up -d

 ✔ Network dev24_bookstore-net Created  # 컨테이너들이 서로 통신할 수 있도록 전용 가상 네트워크를 새로 만든 것                                                                                                                                                                                                                                                                       0.1s
 ✔ Container dev24-nats-1      Healthy # NATS(메시징/이벤트 브로커로 추정) 컨테이너가 생성되어 실행됐고, healthcheck를 통과                                                                                                                                                                                                                                                                         6.4s
 ✔ Container dev24-redis-1     Healthy                                                                                                                                                                                                                                                                          5.9s
 ✔ Container dev24-postgres-1  Healthy                                                                                                                                                                                                                                                                          6.4s
 ✔ Container dev24-app-1       Healthy                                                                                                                                                                                                                                                                         23.2s
 ✔ Container dev24-app-2       Healthy                                                                                                                                                                                                                                                                         23.2s
 ✔ Container dev24-nginx-1     Started # nginx에 의존하는 서비스가 하나도 없어서(체인의 맨 끝이라서). 10~15초 후 ps로 확인해보면 Healthy로 바껴있음

# 3. 상태 확인 - 전부 healthy가 떠야 정상 (app은 2개 컨테이너)
docker compose ps

# 4. Swagger UI 확인 (200 OK)
curl -i http://localhost:8080/swagger-ui/index.html

# 5. actuator가 nginx 밖으로는 막혀 있는지 확인 (404)
curl -i http://localhost:8080/actuator/health

# 6. 컨테이너 내부에서는 실제로 UP인지 확인
docker exec -it $(docker compose ps -q app | head -n1) wget -qO- http://localhost:8080/actuator/health

# 7. 라운드로빈 확인 - 여러 번 요청해서 X-Upstream-Addr이 2개 이상 다른 IP로 나오는지 확인
for i in 1 2 3 4 5 6 7 8; do curl -sI http://localhost:8080/swagger-ui/index.html | grep -i x-upstream-addr; done

# 8. Flyway 마이그레이션 이력 확인
docker exec -it $(docker compose ps -q postgres) psql -U bookstore -d bookstore -c "select * from flyway_schema_history;"

# 9. Redis / NATS 확인
docker exec -it $(docker compose ps -q redis) redis-cli ping
curl -s http://localhost:8222/healthz

# 10. 메모리 제한 준수 확인
docker stats --no-stream

# 11. 정리 (볼륨까지 삭제 - DB 데이터 초기화됨)
docker compose down -v
```

**PowerShell 버전**<br>
bash와 똑같이 쓰면 되는 번호(1, 2, 3, 8의 psql 명령, 9의 redis-cli 명령, 10, 11)는 생략했다.<br>
`curl`/`head`/`grep`이 걸린 번호만 다르다:

```powershell
# 4. Swagger UI 확인 (200 OK) - curl.exe로 진짜 curl을 강제 호출
curl.exe -i http://localhost:8080/swagger-ui/index.html

# 5. actuator가 nginx 밖으로는 막혀 있는지 확인 (404)
curl.exe -i http://localhost:8080/actuator/health

# 6. 컨테이너 내부에서는 실제로 UP인지 확인 (head -n1 -> Select-Object -First 1)
docker exec -it $(docker compose ps -q app | Select-Object -First 1) wget -qO- http://localhost:8080/actuator/health

# 7. 라운드로빈 확인 - grep -> Select-String
1..8 | ForEach-Object { curl.exe -sI http://localhost:8080/swagger-ui/index.html | Select-String -Pattern "x-upstream-addr" }

# 9. NATS 확인 (redis-cli 명령은 bash와 동일)
curl.exe -s http://localhost:8222/healthz

# 11. 정리 (볼륨까지 삭제 - DB 데이터 초기화됨)
docker compose down -v
```

**`-d` 옵션 있고 없고의 차이**: `-d`는 "detached mode"(백그라운드 실행)를 뜻한다.

- `docker compose up` (옵션 없이) — 터미널이 컨테이너들의 로그를 실시간으로 계속 뿌려주는 foreground 모드로 붙어있다. `Ctrl+C`를 누르면 컨테이너들도 같이 멈춘다. 터미널이 로그 출력에 붙잡혀 있어서 그 창으로 다른 명령을 칠 수 없다.
- `docker compose up -d` — 컨테이너들을 띄우고 나서 바로 터미널 제어권을 돌려준다. 터미널을 계속 다른 명령(`docker compose ps`, `curl`, `docker exec` 등)에 쓸 수 있다. 컨테이너는 터미널을 닫아도 계속 떠 있고, 로그를 보려면 따로 `docker compose logs -f`를 실행해야 하며, 멈추려면 `docker compose down`을 명시적으로 실행해야 한다.

위 명령어들이 전부 `-d`를 쓰는 이유도, 띄운 다음 바로 `curl`/`docker exec` 같은 검증 명령을 이어서 칠 수 있게 하려는 것이다.

**`-v` 옵션 있고 없고의 차이**: `-v`는 "volumes"를 뜻한다 — 컨테이너뿐 아니라 compose 파일에서 정의한 볼륨까지 같이 삭제하라는 옵션이다.

- `docker compose down` (옵션 없이) — 컨테이너, 네트워크는 삭제되지만 볼륨(`pgdata`, `natsdata`)은 그대로 남는다. 다음에 `docker compose up`을 하면 Postgres에 이전에 넣었던 데이터(Flyway 마이그레이션 이력 등)가 그대로 살아있다.
- `docker compose down -v` — 컨테이너, 네트워크뿐 아니라 `docker-compose.yml`의 `volumes:` 섹션에 정의된 `pgdata`, `natsdata` 볼륨까지 삭제한다. 다음에 `docker compose up`을 하면 Postgres가 완전히 빈 상태로 다시 시작해서, Flyway가 `V1__init.sql`부터 처음부터 다시 적용된다.

8번 섹션 마지막 정리 명령에 `-v`를 쓴 이유도, 이 가이드를 처음부터 다시 따라 해볼 때 이전 실행의 DB 상태가 남아있지 않도록 완전히 초기화하려는 목적이다.

---

## 9. 문제 해결

| 증상 | 원인 / 해결                                                                                                                                                                                               |
|---|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 포트 충돌 (`Ports are not available` 등) | 로컬에 이미 5432/6379/4222/8222/8080을 쓰는 프로세스가 있는지 확인.<br> `docker compose down` 후 재시도하거나 `docker-compose.yml`의 호스트 포트 숫자를 바꾼다.                                                                            |
| app이 계속 unhealthy로 뜸 | `docker compose logs app`으로 로그 확인.<br> 대개 Postgres/Redis/NATS 연결 실패(호스트명이 `localhost`가 아니라 서비스명 `postgres`/`redis`/`nats`여야 함) 또는 부팅 시간이 `start_period: 30s`보다 오래 걸리는 경우다. 후자는 `start_period` 값을 늘린다.     |
| Flyway 체크섬 불일치 에러 (`FlywayValidateException`) | 이미 적용된 마이그레이션 파일(`V1__init.sql` 등)을 수정했을 때 발생한다.<br> 이미 적용된 파일은 절대 수정하지 말고 새 버전(`V2__...sql`)을 추가한다.<br> 로컬 개발 중 데이터를 버려도 된다면 `docker compose down -v`로 볼륨째 지우고 처음부터 다시 마이그레이션한다.                             |
| `deploy.replicas: 2`인데 컨테이너가 1개만 뜸 | `docker compose version`으로 Compose V2(공백 있는 `docker compose`)를 쓰고 있는지 확인.<br> 구버전 `docker-compose`(하이픈, 파이썬 구현)는 `deploy:` 키를 무시한다.<br> 최신 Docker Desktop을 쓰거나, 임시로 `docker compose up -d --scale app=2`를 쓴다. |
| nginx가 한쪽 컨테이너로만 계속 요청을 보냄 | 5번 섹션에서 설명한 정적 upstream 함정과 동일한 증상이다.<br> `docker/nginx/default.conf`가 `resolver` + 변수 기반 `proxy_pass` 방식인지 확인한다.<br> `upstream { server app:8080; }` 같은 정적 블록으로 되돌아가 있으면 이 문제가 재발한다.                         |
