-- 로그인 비밀번호는 네 회원 모두 12345678이다. 아래에는 미리 만든 BCrypt 해시를 저장한다.
-- password는 평문이 아니며, 로그인할 때 PasswordEncoder가 일치 여부를 확인한다.
INSERT INTO member (name, email, age, password, authority) VALUES
('윤서준', 'SeojunYoon@campus.com', 10, '$2a$10$WNWod6wVX5vhbwJ62XWTDenxLGndTtx5lZswXW4Z/YF8sCoFF22vu', 'ROLE_USER'),
('윤광철', 'KwangcheolYoon@campus.com', 43, '$2a$10$WNWod6wVX5vhbwJ62XWTDenxLGndTtx5lZswXW4Z/YF8sCoFF22vu', 'ROLE_ADMIN'),
('공미영', 'MiyeongKong@campus.com', 23, '$2a$10$WNWod6wVX5vhbwJ62XWTDenxLGndTtx5lZswXW4Z/YF8sCoFF22vu', 'ROLE_USER'),
('김도윤', 'DoyunKim@campus.com', 10, '$2a$10$WNWod6wVX5vhbwJ62XWTDenxLGndTtx5lZswXW4Z/YF8sCoFF22vu', 'ROLE_USER');

-- 관리자가 등록·수정·삭제할 상품 샘플이다.
INSERT INTO product (name, price) VALUES
('아이폰', 1200000),
('맥북', 950000);
