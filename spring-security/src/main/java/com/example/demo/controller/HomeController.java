package com.example.demo.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Slf4j
public class HomeController {
    @GetMapping("/")
    public String home(Authentication authentication) {
        // 로그인했다면 Authentication으로 로그인 ID(이메일)를 확인할 수 있다.
        if (authentication != null) {
            log.info("로그인 이메일: {}", authentication.getName());
        }
        return "home";
    }

    @GetMapping("/login")
    public String login() {
        // GET은 로그인 화면을 보여 주고, POST /login은 Security가 처리한다.
        return "login";
    }

    @GetMapping("/logout")
    public String logout() {
        // 이 요청은 확인 화면만 표시한다. 확인 버튼을 눌러야 실제로 로그아웃된다.
        return "logout";
    }
}
