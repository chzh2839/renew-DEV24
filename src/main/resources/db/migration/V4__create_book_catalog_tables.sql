CREATE TABLE book (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    isbn            VARCHAR(20)  NOT NULL,
    title           VARCHAR(255) NOT NULL,
    authors         VARCHAR(100),
    publisher       VARCHAR(100),
    published_at    DATE,
    price           INT,
    contents        TEXT,
    author_info     TEXT,
    category        VARCHAR(50),
    status          VARCHAR(20)  NOT NULL,
    CONSTRAINT uk_book_isbn UNIQUE (isbn)
);

CREATE TABLE book_image (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    book_id         BIGINT       NOT NULL,
    image_url       VARCHAR(500),
    CONSTRAINT uk_book_image_book_id UNIQUE (book_id),
    CONSTRAINT fk_book_image_book FOREIGN KEY (book_id) REFERENCES book (id)
);

CREATE TABLE rating (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    book_id         BIGINT       NOT NULL,
    rating_sum      INT          NOT NULL DEFAULT 0,
    rating_count    INT          NOT NULL DEFAULT 0,
    sales_count     INT          NOT NULL DEFAULT 0,
    CONSTRAINT uk_rating_book_id UNIQUE (book_id),
    CONSTRAINT fk_rating_book FOREIGN KEY (book_id) REFERENCES book (id)
);
