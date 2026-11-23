package com.example.demo.mapper;

import com.example.demo.model.Member;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MemberMapper {
    List<Member> findAll();
    Member findById(@Param("id") Long id);
    Member findByEmail(@Param("email") String email);
    int insert(@Param("member") Member member);
    int update(@Param("member") Member member);
    int deleteById(@Param("id") Long id);
    List<Member> findAllOrderBy(@Param("order") String order, @Param("dir") String dir);
}
