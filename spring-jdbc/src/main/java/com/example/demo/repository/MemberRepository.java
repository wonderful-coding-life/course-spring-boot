package com.example.demo.repository;

import com.example.demo.model.Member;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class MemberRepository {
    public List<Member> findAll() throws SQLException {
        String url = System.getenv("DB_URL");
        String username = System.getenv("DB_USER");
        String password = System.getenv("DB_PASS");

        String sql = "SELECT * FROM member";

        List<Member> members = new ArrayList<>();
        Connection connection = DriverManager.getConnection(url, username, password);
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);
        // next()는 다음 행으로 이동하며, 읽을 행이 없으면 false를 반환한다.
        while (resultSet.next()) {
            // 조회한 한 행의 값을 Member 객체에 담는다.
            Member member = Member.builder()
                    .id(resultSet.getLong("id"))
                    .name(resultSet.getString("name"))
                    .email(resultSet.getString("email"))
                    .age(resultSet.getInt("age"))
                    .build();
            members.add(member);
        }
        connection.close();

        return members;
    }

    public Member findById(Long id) throws SQLException {
        String url = System.getenv("DB_URL");
        String username = System.getenv("DB_USER");
        String password = System.getenv("DB_PASS");

        String sql = "SELECT * FROM member WHERE id = ?";

        Connection connection = DriverManager.getConnection(url, username, password);
        PreparedStatement statement = connection.prepareStatement(sql);
        // 첫 번째 ?에 회원 ID를 넣는다. 번호는 1부터 시작한다.
        statement.setLong(1, id);

        ResultSet resultSet = statement.executeQuery();

        Member member = null;
        if (resultSet.next()) {
            member = Member.builder()
                    .id(resultSet.getLong("id"))
                    .name(resultSet.getString("name"))
                    .email(resultSet.getString("email"))
                    .age(resultSet.getInt("age"))
                    .build();
        }

        connection.close();
        return member;
    }
}
