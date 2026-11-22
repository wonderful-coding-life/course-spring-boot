package com.example.demo.config;

import com.example.demo.repository.MemberRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // 비밀번호를 해시로 바꾸고, 입력한 비밀번호가 해시와 일치하는지 확인한다.
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(MemberRepository memberRepository) {
        // 로그인 처리는 Security가 담당한다. 여기서는 이메일로 회원 정보를 찾아 제공한다.
        return email -> {
            var member = memberRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("회원을 찾을 수 없습니다."));
            return User.builder()
                    .username(member.getEmail())
                    .password(member.getPassword())
                    .authorities(member.getAuthority())
                    .build();
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(auth -> auth
                        // 오류 응답은 Spring Boot의 /error 처리로 화면을 표시한다.
                        .requestMatchers("/", "/login", "/error").permitAll()
                        // 먼저 일치하는 규칙을 적용한다. 목록 조회는 로그인한 사용자에게 허용한다.
                        .requestMatchers(HttpMethod.GET, "/products").authenticated()
                        // hasRole("ADMIN")은 ROLE_ADMIN 권한을 확인한다.
                        .requestMatchers("/products", "/products/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .formLogin(login -> login
                        .loginPage("/login")
                        // 로그인 폼의 email 입력값을 로그인 ID로 사용한다.
                        .usernameParameter("email")
                        // 로그인에 성공하면 상품 목록으로 이동한다.
                        .defaultSuccessUrl("/products", true)
                        .permitAll())
                .logout(logout -> logout
                        // GET은 확인 화면을 보여 주고, 실제 POST /logout은 Security가 처리한다.
                        .logoutSuccessUrl("/")
                        .permitAll())
                // 일반 화면의 CSRF 보호는 유지한다. POST 폼에는 th:action을 사용한다.
                .build();
    }

}
