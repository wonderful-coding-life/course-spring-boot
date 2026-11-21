package com.example.demo.repository;

import com.example.demo.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Member는 다룰 엔티티, Long은 회원 ID의 타입이다.
 * 아래 기본 메서드는 JpaRepository에서 상속받으므로 따로 선언하지 않아도 된다.
 * - save(member): 새 회원을 저장하거나, 조회한 회원의 정보를 수정해 저장한다.
 * - findById(id): ID로 한 명을 조회한다. 없으면 빈 Optional을 반환한다.
 * - findAll(): 모든 회원을 List로 반환한다.
 * - deleteById(id): 해당 ID의 회원을 삭제한다.
 * - existsById(id): 해당 ID의 회원이 있으면 true, 없으면 false를 반환한다.
 * - count(): 전체 회원 수를 반환한다.
 */
public interface MemberRepository extends JpaRepository<Member, Long> {

    // 아래 메서드는 이름을 보고 Spring Data JPA가 조회 쿼리를 만든다.
    // By 뒤에는 DB 컬럼명이 아닌 Member의 자바 속성명을 쓴다.

    // 이메일이 같은 회원 한 명을 조회한다. 없으면 빈 Optional, 여러 명이면 오류가 발생한다.
    Optional<Member> findByEmail(String email);

    // 이름이 정확히 같은 회원들을 조회한다. 동명이인이 있을 수 있으므로 List로 받는다.
    List<Member> findByName(String name);

    // 이름에 keyword가 포함된 회원들을 조회한다. 예: "윤"이면 "윤서준", "윤광철" 등.
    List<Member> findByNameContaining(String keyword);

    // 나이가 전달한 값 이상인 회원들을 조회한다. 예: 19이면 19세 이상.
    List<Member> findByAgeGreaterThanEqual(Integer age);

    // 이름이 같고(And), 나이가 전달한 값 이상인 회원들을 조회한다.
    List<Member> findByNameAndAgeGreaterThanEqual(String name, Integer age);

    // 같은 이메일의 회원이 있으면 true, 없으면 false를 반환한다.
    boolean existsByEmail(String email);
}
