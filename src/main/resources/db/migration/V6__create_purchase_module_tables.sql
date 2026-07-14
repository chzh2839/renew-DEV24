CREATE TABLE cart (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    customer_id     BIGINT       NOT NULL,
    book_id         BIGINT       NOT NULL,
    quantity        INT          NOT NULL,
    price_snapshot  INT          NOT NULL,
    CONSTRAINT uk_cart_customer_book UNIQUE (customer_id, book_id),
    CONSTRAINT fk_cart_customer FOREIGN KEY (customer_id) REFERENCES customer (id),
    CONSTRAINT fk_cart_book FOREIGN KEY (book_id) REFERENCES book (id)
);

CREATE TABLE purchase (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    customer_id     BIGINT       NOT NULL,
    sender_name     VARCHAR(100) NOT NULL,
    sender_phone    VARCHAR(20)  NOT NULL,
    receiver_name   VARCHAR(100) NOT NULL,
    receiver_phone  VARCHAR(20)  NOT NULL,
    zipcode         VARCHAR(10)  NOT NULL,
    address         VARCHAR(255) NOT NULL,
    payment_method  VARCHAR(20)  NOT NULL,
    total_price     INT          NOT NULL,
    purchased_at    TIMESTAMP    NOT NULL,
    CONSTRAINT fk_purchase_customer FOREIGN KEY (customer_id) REFERENCES customer (id)
);

CREATE TABLE purchase_item (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    purchase_id     BIGINT       NOT NULL,
    book_id         BIGINT       NOT NULL,
    quantity        INT          NOT NULL,
    price           INT          NOT NULL,
    order_state     VARCHAR(20)  NOT NULL,
    CONSTRAINT fk_purchase_item_purchase FOREIGN KEY (purchase_id) REFERENCES purchase (id),
    CONSTRAINT fk_purchase_item_book FOREIGN KEY (book_id) REFERENCES book (id)
);

CREATE TABLE stock (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    book_id         BIGINT       NOT NULL,
    admin_id        BIGINT       NOT NULL,
    quantity        INT          NOT NULL,
    sale_price      INT          NOT NULL,
    safety_stock    INT          NOT NULL DEFAULT 0,
    registered_at   TIMESTAMP    NOT NULL,
    version         INT          NOT NULL DEFAULT 0,
    CONSTRAINT uk_stock_book_id UNIQUE (book_id),
    CONSTRAINT fk_stock_book FOREIGN KEY (book_id) REFERENCES book (id),
    CONSTRAINT fk_stock_admin FOREIGN KEY (admin_id) REFERENCES admin (id)
);
