-- CREATE EXTENSION: PostgreSQL에 외부 기능(확장 프로그램)을 설치
-- pg_trgm: Trigram(3글자)의 약자로, 텍스트를 3글자 단위로 쪼개서(트라이그램) 처리하는 PostgreSQL 기본 확장 모듈.
-- GIN 인덱스가 선행 와일드카드 LIKE/ILIKE 패턴(LOWER(col) LIKE LOWER('%keyword%'))을 지원하도록 해주는 확장이다.
-- 일반 B-tree 인덱스는 좌측부터 일치하는 접두사만 지원해서 이런 패턴을 전혀 처리하지 못한다.
-- BookQueryRepositoryImpl은 QueryDSL containsIgnoreCase()로 키워드 검색을 만드는데, 이게 정확히 이 패턴으로 컴파일된다.
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- keywordContains()에서 OR로 묶인 네 개의 자유 텍스트 컬럼(title, authors, publisher, contents)에 대한 트라이그램 GIN 인덱스.
-- 원본 컬럼이 아니라 lower(col)에 인덱스를 건다: containsIgnoreCase()는 lower(col) LIKE lower('%keyword%')로 컴파일되는데,
-- Postgres는 인덱스의 표현식이 쿼리의 표현식과 정확히 일치해야만 그 인덱스를 사용한다.
-- => 원본 `title` 컬럼에 만든 인덱스는 `lower(title) LIKE ...` 조건에서는 조용히 무시된다.
CREATE INDEX idx_book_title_trgm     ON book USING GIN (lower(title) gin_trgm_ops);
CREATE INDEX idx_book_authors_trgm   ON book USING GIN (lower(authors) gin_trgm_ops);
CREATE INDEX idx_book_publisher_trgm ON book USING GIN (lower(publisher) gin_trgm_ops);
CREATE INDEX idx_book_contents_trgm  ON book USING GIN (lower(contents) gin_trgm_ops);

-- 완전일치 필터(categoryEq()/statusEq())용 복합 B-tree 인덱스.
-- 카탈로그 UI에서는 카테고리별로 둘러보면서 판매중 항목만 보는 식으로 거의 항상 두 조건이 함께 쓰인다.
-- category를 앞에 둔 이유는 일반적인 쿼리 형태에서 더 선택적인(더 좁혀주는) 조건이기 때문이고,
-- status를 뒤에 둬도 category 단독 조건이나 category+status 조합 모두에서 이 인덱스를 그대로 활용할 수 있다.
CREATE INDEX idx_book_category_status ON book (category, status);
