# ERD (To-Be, PostgreSQL)

> 패키지 루트는 `com.dev24.bookstore`로 확정. 아래 스키마는 레거시 Oracle 컬럼(`b_num`, `c_id` 등)을 그대로 복제하지 않고, `DEV24Test`의 VO/매퍼 필드를 근거로 정규화 + 영문 네이밍으로 재설계한 신규 JPA 엔티티 구조다. 실제 Gradle/Spring Boot 프로젝트 생성은 Phase 1에서 진행한다.

## 1. 인증 모듈

레거시 근거: `CustomerVO`(extends `LoginVO`), `AdminVO`.

```mermaid
erDiagram
    CUSTOMER {
        bigint id PK
        varchar login_id UK "레거시 c_id"
        varchar password_hash "레거시 c_passwd 평문 → BCrypt 해시"
        varchar name "레거시 c_name"
        varchar nickname "레거시 c_nickname"
        varchar email "레거시 c_email"
        varchar phone "레거시 c_phone"
        varchar address "레거시 c_address"
        varchar interest "레거시 c_interest"
        boolean newsletter_yn "레거시 c_nletter"
        timestamp joined_at "레거시 c_joinDate"
    }
    ADMIN {
        bigint id PK
        varchar login_id UK "레거시 adm_id"
        varchar password_hash "레거시 adm_passwd 평문 → BCrypt 해시"
        varchar name "레거시 adm_name"
    }
```

Customer/Admin은 별도 테이블로 유지(레거시와 동일한 구분)하되, `Role`(CUSTOMER/ADMIN)을 Spring Security 인가에 사용. 리프레시 토큰/로그아웃 블랙리스트는 DB가 아닌 Redis에 저장(Phase 2).

## 2. 도서 카탈로그 모듈

레거시 근거: `BookVO`, `BookImgVO`, `Rating.xml`(namespace `RatingDAO`, `salescnt` 컬럼).

```mermaid
erDiagram
    BOOK ||--|| BOOK_IMAGE : has
    BOOK ||--|| RATING : has
    BOOK {
        bigint id PK
        varchar name "레거시 b_name"
        varchar author "레거시 b_author"
        varchar publisher "레거시 b_pub"
        date published_at "레거시 b_date"
        int price "레거시 b_price"
        text description "레거시 b_list"
        text author_info "레거시 b_authorinfo"
        varchar state "레거시 b_state: null/unreg/oop → ACTIVE/UNREGISTERED/OUT_OF_PRINT"
        int category_one "레거시 cateOne_num"
        int category_two "레거시 cateTwo_num"
    }
    BOOK_IMAGE {
        bigint id PK
        bigint book_id FK
        varchar list_cover_url "레거시 listcover_imgurl"
        varchar detail_cover_url "레거시 detailcover_imgurl"
        varchar detail_url "레거시 detail_imgurl"
    }
    RATING {
        bigint id PK
        bigint book_id FK
        int rating_sum "레거시 ra_sum"
        int rating_count "레거시 ra_count"
        int sales_count "레거시 salescnt"
    }
```

## 3. 장바구니 / 구매 / 재고 모듈

레거시 근거: `CartVO`, `PurchaseVO`, `PdetailVO`, `StockVO`/`StockDetailVO`, `stock.xml`(테이블 `stock`, `stk_incp`=book 참조, `stk_qty`, `stk_salp`, `adm_num`).

```mermaid
erDiagram
    CUSTOMER ||--o{ CART : owns
    BOOK ||--o{ CART : referenced_by
    CUSTOMER ||--o{ PURCHASE : places
    PURCHASE ||--o{ PURCHASE_ITEM : contains
    BOOK ||--o{ PURCHASE_ITEM : referenced_by
    BOOK ||--|| STOCK : has
    ADMIN ||--o{ STOCK : registers

    CART {
        bigint id PK
        bigint customer_id FK
        bigint book_id FK
        int quantity "레거시 crt_qty"
        int price_snapshot "레거시 crt_price"
    }
    PURCHASE {
        bigint id PK
        bigint customer_id FK
        varchar sender_name "레거시 p_sender"
        varchar sender_phone "레거시 p_senderphone"
        varchar receiver_name "레거시 p_receiver"
        varchar receiver_phone "레거시 p_receivephone"
        varchar zipcode "레거시 p_zipcode"
        varchar address "레거시 p_address"
        varchar payment_method "레거시 p_pmethod"
        int total_price "레거시 p_price"
        timestamp purchased_at "레거시 p_buydate"
    }
    PURCHASE_ITEM {
        bigint id PK
        bigint purchase_id FK
        bigint book_id FK
        int quantity "레거시 pd_qty"
        int price "레거시 pd_price"
        varchar order_state "레거시 pd_orderstate"
    }
    STOCK {
        bigint id PK
        bigint book_id FK "1:1, 레거시 stk_incp"
        int quantity "레거시 stk_qty"
        int sale_price "레거시 stk_salp"
        int safety_stock "신규 필드 — 안전재고 임계치"
        bigint admin_id FK "레거시 adm_num"
        timestamp registered_at "레거시 stk_regdate"
        int version "낙관적 락(@Version), 레거시엔 없음"
    }
```

**신규 설계 포인트**
- `Stock.safety_stock`: 구매 가능 수량은 `quantity - safety_stock`로 검증(단순 재고 있음/없음이 아니라 임계치 기반 판매 가능 여부 판단). 임계치 이하로 떨어지면 `LowStockEvent`를 Kafka로 발행.
- `Stock.version`: 동시 구매 시 오버셀 방지용 낙관적 락. 비관적 락 대신 낙관적 락을 선택한 이유는 `MODERNIZATION_PLAN.md` 3절 참고.
- 구매 완료 트랜잭션 커밋 후 적립금/알림은 `OrderCompletedEvent`로 Kafka에 발행(비동기, 최종적 일관성으로 충분).

## 4. 리뷰 모듈

레거시 근거: `ReviewVO`(extends `CommonVO`, `pd_num` 참조로 실구매 검증 가능한 구조).

```mermaid
erDiagram
    CUSTOMER ||--o{ REVIEW : writes
    BOOK ||--o{ REVIEW : reviewed_by
    PURCHASE_ITEM ||--o| REVIEW : verifies_purchase

    REVIEW {
        bigint id PK
        bigint customer_id FK "레거시 c_num"
        bigint book_id FK "레거시 b_num"
        bigint purchase_item_id FK "레거시 pd_num, 구매 인증 리뷰용"
        int score "레거시 re_score"
        text content "레거시 re_content"
        varchar type "레거시 re_type: text/image"
        varchar image_url "레거시 re_imgurl"
        timestamp written_at "레거시 re_writedate"
    }
```

레거시는 `ReviewVO`에 `ra_num`/`ra_count`(평점 집계)를 함께 들고 있었으나, 신규 설계에서는 집계를 `RATING` 테이블(도서 카탈로그 모듈)로 일원화해 중복을 제거한다.
