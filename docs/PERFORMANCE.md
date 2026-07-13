# 도서 검색 성능: `EXPLAIN ANALYZE` Before/After

관련 계획: [`MODERNIZATION_PLAN.md`](../MODERNIZATION_PLAN.md) Phase 3 (도서 카탈로그 모듈)
마이그레이션: [`V5__add_book_search_index.sql`](../src/main/resources/db/migration/V5__add_book_search_index.sql)

## 한눈에 보기

- **문제**: 도서 검색(`GET /api/books?keyword=...`)이 인덱스 없이 테이블을 처음부터 끝까지 훑고 있었다.
- **해결**: `pg_trgm` 트라이그램 인덱스(키워드 검색용) + 복합 인덱스(category/status 필터용)를 추가했다.
- **결과**: 아래 5개 쿼리 중 4개가 **6배~250배** 빨라졌다. 나머지 1개(Q2)는 원래도 빨라서 인덱스 효과가 없었는데, 그 이유도 함께 기록해뒀다.
- **어떻게 검증했나**: 실제 서비스 데이터(최대 300건)로는 차이가 안 보여서, 합성 데이터 8만 건으로 별도 측정했다. 자세한 이유는 [측정 환경](#측정-환경) 참고.

| 쿼리 | 무엇을 하는 쿼리인가 | Before | After | 개선 |
|---|---|---|---|---|
| Q1 | 키워드로 20건 조회 | 11.694 ms | 1.939 ms | ~6배 |
| Q2 | 카테고리+상태로 20건 조회 | 0.095 ms | 0.108 ms | 변화 없음 (원래 빠름) |
| Q2b | 카테고리+상태 전체 건수 세기 | 22.453 ms | 0.860 ms | ~26배 |
| Q3 | 키워드+카테고리+상태로 20건 조회 | 25.151 ms | 0.616 ms | ~41배 |
| Q4 | 키워드 전체 건수 세기 | 145.638 ms | 0.583 ms | ~250배 |

## 배경: 왜 인덱스가 필요했나

`BookQueryRepositoryImpl.keywordContains()`는 QueryDSL `containsIgnoreCase()`로 검색어를 다음과 같은 SQL로 컴파일한다.

```sql
WHERE lower(title) LIKE lower('%keyword%')
   OR lower(authors) LIKE lower('%keyword%')
   OR lower(publisher) LIKE lower('%keyword%')
   OR lower(contents) LIKE lower('%keyword%')
```

`%keyword%`처럼 앞에 `%`가 붙는 LIKE 패턴은 일반 B-tree 인덱스로는 지원할 수 없다(B-tree는 "가나다..." 순으로 정렬된 목차를 앞에서부터 찾아가는 방식이라, 단어가 중간 어디에 있을지 모르는 검색에는 쓸 수 없다).<br>
이 문제를 해결하려면 PostgreSQL의 `pg_trgm` 확장과 트라이그램 GIN 인덱스가 필요하다 — 문자열을 3글자 단위 조각(트라이그램)으로 미리 쪼개 색인해두는 방식이라 "중간에 이 글자가 있는가"도 빠르게 찾을 수 있다. `category`/`status`는 완전히 같은 값인지만 보는 조건(`eq()`)이라 일반 B-tree 인덱스로 충분하다.

## `EXPLAIN ANALYZE` 결과를 읽는 법

아래 결과에 나오는 실행계획 용어를 미리 알아두면 편하다.

| 용어 | 의미 |
|---|---|
| **Seq Scan** | 테이블을 처음부터 끝까지 한 행씩 다 훑어보는 것. 인덱스가 없거나 못 쓸 때 벌어진다. 테이블이 크면 클수록 느려진다. |
| **Index Scan** | 인덱스를 이용해 조건에 맞을 만한 행을 찾아가는 것. `book_pkey`(기본키) 인덱스를 쓰면 `id` 순서대로 훑을 수 있다는 뜻. |
| **Bitmap Index Scan / Bitmap Heap Scan** | 인덱스로 "몇 번째 행들이 조건에 맞는지" 목록을 먼저 만든 다음(Bitmap Index Scan), 그 목록에 있는 행들만 골라서 읽는 것(Bitmap Heap Scan). 조건에 맞는 행이 흩어져 있을 때 효율적이다. |
| **BitmapOr** | 여러 인덱스의 결과(예: title에서 찾은 것 OR authors에서 찾은 것)를 하나로 합치는 것. |
| **Index Only Scan** | 테이블 원본을 아예 읽지 않고 인덱스만 봐도 답이 나오는 가장 빠른 경우. |
| **actual time / Execution Time** | 실제로 걸린 시간(ms). 이 문서에서 "Before/After"로 비교하는 핵심 숫자. |
| **Rows Removed by Filter** | 조건에 안 맞아서 버려진 행 수. 이 숫자가 크면 클수록 "쓸데없이 많이 훑었다"는 뜻. |

아래 각 쿼리마다 "Before/After 원문 실행계획 + 한 줄 요약"을 붙여뒀다. 원문을 다 읽지 않아도 요약만으로 무슨 일이 있었는지 알 수 있게 했다.

## 측정 환경

실제 운영 시딩(`BookSeedService`, Kakao API 기반 6개 키워드)은 최대 300건 규모다. 테이블이 이 정도로 작으면 Postgres는 인덱스가 있든 없든 그냥 전체를 다 읽어버리는 쪽(Seq Scan)을 택한다 — 어차피 몇 페이지 안 되니 인덱스를 거치는 게 오히려 손해이기 때문이다. 그래서 인덱스 효과를 보려면 데이터를 훨씬 크게 불려서 측정해야 했다.

- `postgres:16-alpine` 이미지로 **측정 전용 컨테이너**를 별도로 띄웠다(실제 운영 시딩 파이프라인 `BookSeedRunner`/`app.book-seed.enabled`는 전혀 건드리지 않았고, 측정 후 컨테이너와 볼륨을 전량 폐기했다).
- 합성 데이터 **80,000행**을 생성했다. `title`/`authors`/`publisher`/`category`/`status` 값은 무작위로 채워서, 실제 서비스처럼 검색어에 매칭되는 행들이 테이블 여기저기 흩어지도록 했다.
- 검색 키워드는 "쿠버네티스"로 정하고, 전체 80,000행 중 **260행(0.325%)** 에만 등장하도록 의도적으로 희소하게 만들었다.
  > 처음엔 모든 행의 `contents`에 똑같은 문구가 들어가는 바람에 검색어가 100% 행에 다 매칭되는 실수를 했다 — 이러면 인덱스가 있어도 어차피 거의 다 읽어야 하니 Before/After 차이가 안 보인다. 데이터를 다시 만들어 바로잡았다.
- `category='프로그래밍' AND status='ACTIVE'` 조건은 5,997행(7.5%)이 매칭되도록 했다.
- 절차: `book` 테이블(V4 스키마와 동일) 생성 → 합성 데이터 INSERT → `ANALYZE`(통계 갱신) → **Before** 쿼리 실행 → `V5__add_book_search_index.sql` 적용 → `ANALYZE` → **After** 쿼리 재실행.

## 결과

### Q1. 키워드 단독 검색 (title/authors/publisher/contents 중 하나라도 매칭), 20건만 조회

**Before 요약**: 인덱스가 없어서 조건에 맞는 20건을 찾으려고 `id` 순서대로 5,772행을 하나씩 확인했다.
```
Limit  (cost=0.29..4155.29 rows=20 width=260) (actual time=0.406..11.584 rows=20 loops=1)
  ->  Index Scan using book_pkey on book  (cost=0.29..6648.29 rows=32 width=260) (actual time=0.406..11.571 rows=20 loops=1)
        Filter: ((lower(title) ~~ '%쿠버네티스%') OR (lower(authors) ~~ '%쿠버네티스%') OR (lower(publisher) ~~ '%쿠버네티스%') OR (lower(contents) ~~ '%쿠버네티스%'))
        Rows Removed by Filter: 5752
Execution Time: 11.694 ms
```

**After 요약**: 트라이그램 인덱스 4개(`idx_book_*_trgm`)가 각자 후보를 찾아서(`BitmapOr`) 합친 뒤, 그 행들만 읽었다.
```
Limit  (cost=234.78..234.82 rows=16 width=259) (actual time=1.572..1.576 rows=20 loops=1)
  ->  Sort ...
        ->  Bitmap Heap Scan on book  (cost=173.51..234.46 rows=16 width=259) (actual time=0.168..1.486 rows=260 loops=1)
              Recheck Cond: (...)
              ->  BitmapOr
                    ->  Bitmap Index Scan on idx_book_title_trgm ...
                    ->  Bitmap Index Scan on idx_book_authors_trgm ...
                    ->  Bitmap Index Scan on idx_book_publisher_trgm ...
                    ->  Bitmap Index Scan on idx_book_contents_trgm ...
Execution Time: 1.939 ms
```
**결과**: 11.694ms → 1.939ms, 약 **6배** 개선.

### Q2. `category + status` 완전일치 필터, 20건만 조회

**미리 알아둘 점**: 이 쿼리는 인덱스를 추가해도 속도가 그대로다 — 왜 그런지 아래에서 설명한다.

```
-- Before, After 실행계획이 동일함
Limit  (cost=0.29..18.4x rows=20 ...) (actual time=0.02x..0.0xx rows=20 loops=1)
  ->  Index Scan using book_pkey on book ...
        Filter: (category = '프로그래밍' AND status = 'ACTIVE')
Execution Time: Before 0.095 ms / After 0.108 ms (오차 범위, 사실상 변화 없음)
```
**왜 변화가 없었나**:<br>
이 조건은 전체의 7.5%나 되고, "20건만" + "`id` 순서대로" 조회하면 되므로, 인덱스가 없어도 기본키 순서로 조금만 훑으면 20건이 금방 채워진다. 그래서 새로 만든 `idx_book_category_status`가 있어도 옵티마이저는 굳이 그걸 쓰지 않고 기본키 스캔을 그대로 선택했다.<br>
**인덱스를 만든다고 모든 쿼리가 빨라지는 건 아니라는 걸 보여주는 사례**로 남겨둔다. 이 인덱스가 실제로 쓸모 있는 경우는 바로 다음(Q2b)이다.

### Q2b. 동일 필터의 전체 건수 세기(`count(*)`) — Q2와 조건은 같지만 "20건만"이라는 조건이 없음

목록 화면은 "전체 몇 건 중 몇 페이지"를 보여줘야 해서, `BookQueryRepositoryImpl.search()`는 위 Q2와 같은 조건으로 `count(*)` 쿼리도 따로 실행한다. 이번엔 "20건만 채우면 끝"이 아니라 **조건에 맞는 행을 전부 세어야** 하므로, Q2처럼 일찍 멈출 수가 없다.

**Before 요약**: 전체 8만 행을 다 읽으면서 조건에 맞는지 하나하나 확인했다(그중 74,003건은 버림).
```
Aggregate (cost=4175.93..4175.94 rows=1 width=8) (actual time=22.410..22.411 rows=1 loops=1)
  ->  Seq Scan on book  (actual time=0.008..22.143 rows=5997 loops=1)
        Filter: (category = '프로그래밍' AND status = 'ACTIVE')
        Rows Removed by Filter: 74003
Execution Time: 22.453 ms
```
**After 요약**: 인덱스만 보고 바로 건수를 세서, 테이블 원본은 읽지도 않았다(가장 빠른 유형인 Index Only Scan).
```
Aggregate (cost=158.69..158.69 rows=1 width=8) (actual time=0.796..0.796 rows=1 loops=1)
  ->  Index Only Scan using idx_book_category_status on book  (actual time=0.110..0.540 rows=5997 loops=1)
        Index Cond: (category = '프로그래밍' AND status = 'ACTIVE')
        Heap Fetches: 0
Execution Time: 0.860 ms
```
**결과**: 22.453ms → 0.860ms, 약 **26배** 개선.

### Q3. 키워드 + category + status 결합, 20건만 조회

**Before 요약**: 세 조건을 모두 만족하는 행이 16건(전체의 0.02%)뿐이라 너무 희소해서, 기본키 순서로 조금만 훑는 전략이 더 이상 안 통했다. 결국 여러 CPU 코어를 나눠 쓰는 전체 스캔(Parallel Seq Scan)으로 전환됐다.
```
Limit (cost=5608.08..5608.20 rows=1 width=260) (actual time=21.492..24.974 rows=16 loops=1)
  ->  Gather Merge (Workers Launched: 1)
        ->  Sort
              ->  Parallel Seq Scan on book (actual time=1.914..17.676 rows=8 loops=2)
                    Rows Removed by Filter: 39992
Execution Time: 25.151 ms
```
**After 요약**: 트라이그램 인덱스로 키워드 후보를 먼저 좁힌 뒤, category/status는 그 결과 안에서만 다시 확인했다.
```
Limit (cost=234.54..234.54 rows=1 width=259) (actual time=0.497..0.500 rows=16 loops=1)
  ->  Sort
        ->  Bitmap Heap Scan on book (actual time=0.138..0.475 rows=16 loops=1)
              Recheck Cond: (키워드 조건)
              Filter: (category = '프로그래밍' AND status = 'ACTIVE')
              ->  BitmapOr (title/authors/publisher/contents trgm 인덱스)
Execution Time: 0.616 ms
```
**결과**: 25.151ms → 0.616ms, 약 **41배** 개선.

### Q4. 키워드 검색의 전체 건수 세기(`count(*)`) — Q1과 조건은 같지만 "20건만"이라는 조건이 없음

**Before 요약**: "몇 건 있는지 정확히 세야" 하니 20건 채우고 멈출 수 없어서, 8만 행 전체를 처음부터 끝까지 다 읽었다. 그래서 이 문서에서 가장 느린 케이스가 됐다.
```
Aggregate (cost=5361.08..5361.09 rows=1 width=8) (actual time=145.619..145.620 rows=1 loops=1)
  ->  Seq Scan on book (actual time=0.373..145.563 rows=260 loops=1)
        Rows Removed by Filter: 79740
Execution Time: 145.638 ms
```
**After 요약**: 트라이그램 인덱스로 260건의 후보만 바로 찾아서 그것만 읽었다.
```
Aggregate (cost=234.50..234.51 rows=1 width=8) (actual time=0.478..0.479 rows=1 loops=1)
  ->  Bitmap Heap Scan on book (actual time=0.128..0.463 rows=260 loops=1)
        ->  BitmapOr (title/authors/publisher/contents trgm 인덱스)
Execution Time: 0.583 ms
```
**결과**: 145.638ms → 0.583ms, 약 **250배** 개선 — "LIMIT이 없어서 일찍 멈출 수 없는 쿼리"일수록 인덱스 효과가 가장 크게 나타난다는 걸 보여준다.

## 진단 중 발견한 실수 (그대로 기록)

처음엔 트라이그램 인덱스를 `title`, `authors` 같은 **원본 컬럼**에 만들었다(`GIN (title gin_trgm_ops)`). 그 상태로 다시 측정했더니 Q1/Q4가 인덱스를 만든 후에도 실행계획이 하나도 안 바뀌었다.

원인은 QueryDSL이 만드는 조건이 `lower(title) LIKE ...`인데, Postgres는 **인덱스가 정확히 같은 표현식(`lower(title)`)으로 만들어져 있어야만** 그 인덱스를 쓴다는 점이었다. 원본 컬럼(`title`)에 만든 인덱스는 `lower(title)` 조건에는 아예 후보로 고려되지 않는다.

`CREATE INDEX ... USING GIN (lower(title) gin_trgm_ops)`처럼 **`lower()`를 씌운 표현식 인덱스**로 바꾸자 그제서야 `BitmapOr`로 인덱스를 타기 시작했다. 최종 마이그레이션(`V5__add_book_search_index.sql`)은 이 표현식 인덱스 버전이다.

## 정리

| 쿼리 | Before 플랜 | After 플랜 | Before | After | 개선 |
|---|---|---|---|---|---|
| Q1. 키워드 검색 (20건 조회) | Index Scan(PK)+Filter | BitmapOr(trgm)+Bitmap Heap Scan | 11.694 ms | 1.939 ms | ~6배 |
| Q2. category+status (20건 조회) | Index Scan(PK)+Filter | Index Scan(PK)+Filter (동일) | 0.095 ms | 0.108 ms | 변화 없음 |
| Q2b. category+status 전체 건수 | Seq Scan | Index Only Scan | 22.453 ms | 0.860 ms | ~26배 |
| Q3. 키워드+category+status (20건 조회) | Parallel Seq Scan | Bitmap Heap Scan | 25.151 ms | 0.616 ms | ~41배 |
| Q4. 키워드 검색 전체 건수 | Seq Scan | Bitmap Heap Scan | 145.638 ms | 0.583 ms | ~250배 |

핵심 교훈 두 가지:
1. **`LIMIT`이 있고 정렬 기준(`id`)이 기본키와 같은 쿼리(Q2)** 는 인덱스가 없어도 이미 빠를 수 있다 — 조건에 맞는 행이 어느 정도 있으면, 그냥 기본키 순서로 조금만 훑어도 원하는 개수를 금방 채우기 때문이다.
2. 반대로 **`LIMIT` 없이 전체 건수를 세야 하는 페이징용 `count()` 쿼리(Q2b, Q4)** 는 일찍 멈출 방법이 없어서, 인덱스 효과가 가장 크게(26배~250배) 나타난다.

---

# Redis 캐시 히트/미스 응답시간 비교

관련 코드: `BookCacheConfig`, `BookQueryService.search()`/`getDetail()`(`@Cacheable`)

## 배경

도서 목록(`GET /api/books`)/상세(`GET /api/books/{id}`) 조회에 Redis 기반 `@Cacheable`을 적용했다. `BookQueryRepositoryNPlusOneTest`류 테스트로 "캐시 히트 시 SQL이 추가로 안 나간다"는 건 이미 증명했지만, 이번엔 **실제 HTTP 응답시간**이 얼마나 달라지는지를 실측한다 — 쿼리 실행 횟수와 실제 체감 응답속도는 별개의 증거다.

## 측정 환경/방법

- `docker compose up -d postgres redis`로 띄운 실제 Postgres/Redis + `./gradlew bootRun`으로 로컬 실행한 실제 앱(포트 8080). `/api/books/**`는 `SecurityConfig`에서 `permitAll`이라 토큰 없이 바로 호출 가능.
- `.env`의 실제 카카오 API 키로 `APP_BOOK_SEED_ENABLED=true` 시딩 → 실제 도서 299건 적재됨(로그: `도서 카탈로그 시딩 완료 - 299건 적재`).
- **측정 전 워밍업**:<br>
  - 관련 없는 요청 여러 번을 먼저 날려 JVM JIT/HikariCP 커넥션 풀을 데웠다. 이걸 안 하면 "미스" 쪽 수치에 JVM 콜드스타트 비용이 섞여, 캐시 효과가 아니라 애플리케이션 기동 직후 효과를 재는 셈이 되어 버린다.
- **미스(miss) 측정**:<br>
  - 캐시에 없는 새 키로 10회 호출 — 상세 조회는 서로 다른 도서 id 10개, 목록 검색은 실제로 존재하지 않는 `category` 값을 매번 새로 만들어(`category=nonexistent-1`, `-2`, ...) 매번 캐시 미스가 되도록 함(존재하지 않는 값이어도 실제 WHERE 조건으로 DB를 스캔하는 진짜 쿼리이므로 측정 목적엔 문제없음).
- **히트(hit) 측정**:<br>
  - 하나의 키(도서 id 하나 / 목록 검색 조건 하나)를 한 번 호출해 캐시를 채운 뒤, 같은 키로 10회 더 호출.
- `curl -s -o /dev/null -w "%{time_total}"`로 순수 응답시간(초)만 측정.<br>
첫 측정 시도에서 미스/히트를 1:1로 번갈아 측정했더니 로컬 환경 특성상 각 호출이 수 ms~수십 ms 단위라 시스템 노이즈(OS 스케줄링 등)에 묻혀 값이 들쭉날쭉했다 — 그래서 "미스 10회 vs 히트 10회"를 각각 몰아서 측정하고 평균을 비교하는 방식으로 바꿨다.

## 결과

### 상세 조회 (`GET /api/books/{id}`)

```
미스(서로 다른 id 10개): 0.0136 0.0132 0.0102 0.0125 0.0116 0.0120 0.0111 0.0109 0.0103 0.0098 (초)
히트(같은 id 10회):      0.0059 0.0062 0.0071 0.0064 0.0067 0.0061 0.0067 0.0061 0.0059 0.0064 (초)
```
- 미스 평균: **11.5 ms**
- 히트 평균: **6.4 ms**
- 약 **1.8배** 개선

### 목록 검색 (`GET /api/books?category=...`)

```
미스(매번 새로운 category 값 10회): 0.0182 0.0123 0.0113 0.0115 0.0120 0.0115 0.0127 0.0114 0.0118 0.0113 (초)
히트(같은 조건 10회):               0.0022 0.0024 0.0019 0.0018 0.0023 0.0025 0.0027 0.0023 0.0024 0.0024 (초)
```
- 미스 평균: **12.4 ms**
- 히트 평균: **2.3 ms**
- 약 **5.4배** 개선

## 해석

- 목록 검색이 상세 조회보다 캐시 효과가 더 컸다(5.4배 vs 1.8배).<br>
상세 조회는 이미 인덱스(PK)로 단일 행만 가져오는 가벼운 쿼리라 DB 조회 자체가 원래도 빨랐던 반면(Q2가 인덱스 유무와 무관하게 빨랐던 것과 같은 이유), 목록 검색은 QueryDSL 동적 조건 + 두 개의 fetch join을 매번 다시 계산해야 해서 DB 왕복 비용이 상대적으로 더 크고, 그만큼 캐시로 건너뛸 때 이득도 크다.
- 두 경우 모두 절대 시간 자체는 로컬 환경 기준 수 ms~십수 ms 수준으로 작다<br>
— 도서 299건짜리 로컬 Postgres라 원래도 매우 빠르기 때문이다. 데이터 규모가 커지거나 네트워크를 거치는 실제 운영 환경에서는 캐시로 건너뛰는 DB 왕복 비용 자체가 커지므로 절대적인 개선폭은 더 커질 것으로 예상된다(이 문서의 목적은 "캐시가 실제로 DB 접근을 건너뛰어 응답시간을 줄인다"는 것을 이 환경에서 실측으로 증명하는 것이지, 프로덕션 규모의 정확한 배수를 예측하는 것은 아니다).
