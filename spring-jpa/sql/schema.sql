CREATE TABLE article (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(256),
    description VARCHAR(4096),
    created TIMESTAMP,
    updated TIMESTAMP,
    member_id BIGINT,
    FOREIGN KEY (member_id) REFERENCES member(id)
);
