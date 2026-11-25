package com.example.demo.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.NoSuchElementException;

// 여러 Controller에서 발생한 예외를 한곳에서 처리한다.
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBadRequest(Model model) {
        // /members/abc처럼 숫자인 ID에 문자를 보내면 400으로 응답한다.
        model.addAttribute("status", 400);
        model.addAttribute("message", "회원 ID 등 숫자로 입력해야 하는 값을 확인하세요.");
        return "error";
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(NoSuchElementException ex, Model model) {
        // @ResponseStatus는 실제 HTTP 응답을 404로 설정한다.
        // Model의 status와 message는 오류 화면에 표시할 값이다.
        model.addAttribute("status", 404);
        model.addAttribute("message", ex.getMessage());
        return "error";
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleResourceNotFound(Model model) {
        // 정적 파일 누락은 서버 오류가 아니므로 404로 응답하고 오류 로그를 남기지 않는다.
        model.addAttribute("status", 404);
        model.addAttribute("message", "요청한 파일을 찾을 수 없습니다.");
        return "error";
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleConflict(DataIntegrityViolationException ex, Model model) {
        // 중복 이메일 등 DB 제약 위반은 409로 응답한다. 상세 원인은 로그에 남긴다.
        log.error("데이터 제약 위반으로 요청을 처리하지 못했습니다.", ex);
        model.addAttribute("status", 409);
        model.addAttribute("message", "데이터 제약으로 요청을 처리할 수 없습니다. 중복 이메일 등을 확인하세요.");
        return "error";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleException(Exception ex, Model model) {
        // 예상하지 못한 오류는 자세한 내용을 로그에 남기고 공통 안내를 표시한다.
        log.error("요청 처리 중 예상하지 못한 오류가 발생했습니다.", ex);
        model.addAttribute("status", 500);
        model.addAttribute("message", "처리 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");
        return "error";
    }
}
