CREATE TABLE catalog
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    created_at datetime     NOT NULL,
    updated_at datetime     NOT NULL,
    name       VARCHAR(100) NOT NULL,
    CONSTRAINT pk_catalog PRIMARY KEY (id)
);

CREATE TABLE product_image
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    created_at datetime NOT NULL,
    updated_at datetime NOT NULL,
    url        VARCHAR(255) NULL,
    product_id BIGINT   NOT NULL,
    CONSTRAINT pk_product_image PRIMARY KEY (id)
);

ALTER TABLE product_image
    ADD CONSTRAINT uc_f615143f07e69e11286c8f7d5 UNIQUE (url, product_id);

ALTER TABLE product_image
    ADD CONSTRAINT FK_PRODUCT_IMAGE_ON_PRODUCT FOREIGN KEY (product_id) REFERENCES products (id);

CREATE INDEX idx_product_image_product ON product_image (product_id);

CREATE TABLE products
(
    id            BIGINT AUTO_INCREMENT NOT NULL,
    created_at    datetime     NOT NULL,
    updated_at    datetime     NOT NULL,
    name          VARCHAR(255) NOT NULL,
    `description` TEXT         NOT NULL,
    price         DECIMAL(15,2)      NOT NULL,
    is_selling    TINYINT(1)       NOT NULL,
    catalog_id    BIGINT NULL,
    CONSTRAINT pk_products PRIMARY KEY (id)
);
ALTER TABLE products
    ADD CONSTRAINT FK_PRODUCTS_ON_CATALOG FOREIGN KEY (catalog_id) REFERENCES catalog (id);

ALTER TABLE products ADD FULLTEXT(name, description);

CREATE INDEX idx_product_name ON products (name);

CREATE INDEX idx_product_catalog ON products (catalog_id);

CREATE TABLE sale_offs
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    created_at datetime NOT NULL,
    updated_at datetime NOT NULL,
    start_date timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    end_date   timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    discount   DECIMAL(15, 2)  NOT NULL,
    is_active  TINYINT(1)   NOT NULL,
    product_id BIGINT   NOT NULL,
    CONSTRAINT pk_sale_offs PRIMARY KEY (id)
);

CREATE INDEX idx_sale_off_product_time_active ON sale_offs (product_id, is_active, start_date, end_date);

ALTER TABLE sale_offs
    ADD CONSTRAINT FK_SALE_OFFS_ON_PRODUCT FOREIGN KEY (product_id) REFERENCES products (id);

