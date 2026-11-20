package com.example.demo.mapper;

import com.example.demo.model.Member;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MemberMapper {
    List<Member> findAll();
    Member findById(Long id);
    Member findByEmail(String email);
    int insert(Member member);
    int update(Member member);
    int deleteById(Long id);
    List<Member> findAllOrderBy(@Param("order") String order, @Param("dir") String dir);
}
