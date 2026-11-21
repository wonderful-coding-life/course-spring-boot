package com.example.demo.controller;

import com.example.demo.model.Member;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

// @Controller는 JSON 대신 HTML 화면을 응답할 때 사용한다.
@Controller
public class ThymeleafController {

    @GetMapping("/")
    public String index() {
        // 반환한 이름에 해당하는 templates/index.html을 사용한다.
        return "index";
    }

    @GetMapping("/text")
    public String text(Model model) {
        // Model에 넣은 값은 HTML에서 같은 이름으로 사용할 수 있다.
        model.addAttribute("name", "윤서준");
        model.addAttribute("member", sampleMembers().getFirst());
        return "text";
    }

    @GetMapping("/condition")
    public String condition(Model model) {
        // 회원의 나이를 20 이상으로 바꾸면 표시되는 문구가 달라진다.
        model.addAttribute("member", sampleMembers().getFirst());
        return "condition";
    }

    @GetMapping("/list")
    public String list(Model model) {
        model.addAttribute("members", sampleMembers());
        return "list";
    }

    @GetMapping("/link")
    public String link(@RequestParam(name = "id", defaultValue = "1") Long id, Model model) {
        model.addAttribute("members", sampleMembers());
        // 링크의 id에 해당하는 회원을 화면에 전달한다.
        for (Member member : sampleMembers()) {
            if (member.getId().equals(id)) {
                model.addAttribute("member", member);
                break;
            }
        }
        return "link";
    }

    @GetMapping("/form")
    public String form(Model model) {
        // 빈 객체를 전달하면 th:field로 입력칸을 연결할 수 있다.
        model.addAttribute("member", new Member());
        return "form";
    }

    @PostMapping("/form")
    public String submit(@ModelAttribute("member") Member member) {
        // 폼의 name, email, age를 객체에 담아 결과 화면에 전달한다. DB에는 저장하지 않는다.
        return "result";
    }

    @GetMapping("/utility")
    public String utility(Model model) {
        model.addAttribute("now", LocalDateTime.now());
        return "utility";
    }

    // 각 실습에서 사용할 회원 4명을 만든다. DB 연결은 필요하지 않다.
    private List<Member> sampleMembers() {
        return List.of(
                Member.builder().id(1L).name("윤서준").email("SeojunYoon@campus.com").age(10).build(),
                Member.builder().id(2L).name("윤광철").email("KwangcheolYoon@campus.com").age(43).build(),
                Member.builder().id(3L).name("공미영").email("MiyeongKong@campus.com").age(23).build(),
                Member.builder().id(4L).name("김도윤").email("DoyunKim@campus.com").age(10).build()
        );
    }
}
