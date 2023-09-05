package com.changddao.demo;

import com.changddao.demo.entity.Member;
import com.changddao.demo.entity.QMember;
import com.changddao.demo.entity.Team;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.changddao.demo.entity.QMember.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
public class QueryDslBasicTest {
    @PersistenceContext
    EntityManager em;
    JPAQueryFactory queryFactory;

    @BeforeEach
    public void before(){
        queryFactory = new JPAQueryFactory(em);
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

        //init
        em.flush();
        em.clear();
    }
    @Test
    public void startJPQL(){
    //given
        Member findMember = em.createQuery("select m from Member m where m.username = :username", Member.class)
                .setParameter("username", "member1").getSingleResult();


        //when
        assertThat(findMember.getAge()).isEqualTo(10);



    //then


    }
    @Test
    public void startQueryDsl(){
    //given

        Member findMember = queryFactory.selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();


        //when
        assertThat(findMember.getUsername()).isEqualTo("member1");



    //then


    }
    @Test
    public void search(){
    //given
        List<Member> findByCase = queryFactory.selectFrom(member)
                .where(member.age.lt(20))
                .fetch();


        //when
        assertThat(findByCase.get(0).getUsername()).isEqualTo("member1");



    //then


    }
}
