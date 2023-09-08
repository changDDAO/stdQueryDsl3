package com.changddao.demo.repository;

import com.changddao.demo.dto.MemberSearchCondition;
import com.changddao.demo.dto.MemberTeamDto;
import com.changddao.demo.entity.Member;
import com.changddao.demo.entity.Team;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.aspectj.lang.annotation.Before;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
@Transactional
class MemberJpaRepositoryTest {

    @PersistenceContext
    EntityManager em;
    @Autowired
    MemberJpaRepository jpaRepository;

    @Test
    public void searchTest(){
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1",10, teamA);
        Member member2 = new Member("member2",20, teamA);
        Member member3 = new Member("member3",30, teamB);
        Member member4 = new Member("member4",40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);



    //when
        MemberSearchCondition searchCondition = new MemberSearchCondition();
        searchCondition.setAgeGoe(35);
        searchCondition.setAgeLoe(40);
        searchCondition.setTeamName("teamB");

        List<MemberTeamDto> result = jpaRepository.searchbyBuilder(searchCondition);


        //then
        Assertions.assertThat(result).extracting("username")
                .containsExactly("member4");


    }
}