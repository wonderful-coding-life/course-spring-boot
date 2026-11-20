package com.example.demo;

import com.example.demo.repository.MemberRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.sql.SQLException;

@SpringBootApplication
@Slf4j
public class DemoApplication {

	public static void main(String[] args) throws SQLException {
		var context = SpringApplication.run(DemoApplication.class, args);
		var repository = context.getBean(MemberRepository.class);

		var members = repository.findAll();
		log.info("모든 회원: {}", members);

		var member = repository.findById(1L);
		log.info("1번 회원: {}", member);
	}

}
