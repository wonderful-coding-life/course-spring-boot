package com.example.demo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// 테스트용 H2 설정으로 스프링 컨테이너가 정상적으로 시작되는지 확인한다.
@SpringBootTest
class DemoApplicationTests {

	@Test
	@DisplayName("스프링 컨테이너가 정상적으로 시작된다")
	void testContextLoads() {
	}

}
