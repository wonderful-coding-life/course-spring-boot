package com.example.demo;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// 전체 애플리케이션과 DB 설정을 로딩하므로 통합 테스트로 분류한다.
@Tag("integration")
@SpringBootTest
class DemoApplicationTests {

	@Test
	void contextLoads() {
	}

}
