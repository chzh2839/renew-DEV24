CREATE TABLE review (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    customer_id       BIGINT       NOT NULL,
    book_id           BIGINT       NOT NULL,
    purchase_item_id  BIGINT       NOT NULL,
    score             INT          NOT NULL,
    content           TEXT         NOT NULL,
    type              VARCHAR(20)  NOT NULL,
    image_url         VARCHAR(255),
    written_at        TIMESTAMP    NOT NULL,
    CONSTRAINT uk_review_purchase_item UNIQUE (purchase_item_id),
    CONSTRAINT fk_review_customer FOREIGN KEY (customer_id) REFERENCES customer (id),
    CONSTRAINT fk_review_book FOREIGN KEY (book_id) REFERENCES book (id),
    CONSTRAINT fk_review_purchase_item FOREIGN KEY (purchase_item_id) REFERENCES purchase_item (id)
);
