package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
class PasswordEncoderTests {
    @Test
    void encodeAndMatch() {
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        // 같은 비밀번호도 실행할 때마다 다른 해시가 만들어질 수 있다.
        String encoded = encoder.encode("12345678");
        log.info("비밀번호 해시: {}", encoded);
        // 해시 문자열을 직접 비교하지 않고 matches로 비밀번호가 맞는지 확인한다.
        log.info("같은 비밀번호 확인: {}", encoder.matches("12345678", encoded));
        log.info("다른 비밀번호 확인: {}", encoder.matches("wrong-password", encoded));
    }
}
