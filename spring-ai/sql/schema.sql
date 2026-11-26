-- DB_URL에 지정한 MariaDB에서 이 파일을 실행한 뒤 data.sql을 실행합니다.
-- Session과 벡터 저장소 테이블은 애플리케이션 설정에 따라 별도로 생성됩니다.
-- IF NOT EXISTS는 이미 존재하는 테이블의 컬럼이나 제약 조건을 변경하지 않습니다.
CREATE TABLE IF NOT EXISTS product_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_number VARCHAR(255) NOT NULL UNIQUE,
    product_name VARCHAR(255) NOT NULL,
    shipping_address VARCHAR(255) NOT NULL,
    shipping_status VARCHAR(255) NOT NULL,
    member_name VARCHAR(255) NOT NULL
);
