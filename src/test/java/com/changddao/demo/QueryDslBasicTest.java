package com.changddao.demo;

import com.changddao.demo.entity.Member;
import com.changddao.demo.entity.QMember;
import com.changddao.demo.entity.QTeam;
import com.changddao.demo.entity.Team;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
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
import static com.changddao.demo.entity.QTeam.*;
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
    /*
    * 회원 정렬 순서
    * 1. 회원나이 desc
    * 2. 회원 이름 asc
    * 단, 2에서 회원 이름이 없으면 마지막에 출력(nulls last)
    * */
    @Test
    public void sort(){
    //given
    em.persist(new Member(null, 100));
    em.persist(new Member("member5", 100));
    em.persist(new Member("member6", 100));

        List<Member> result = queryFactory.selectFrom(member)
                .orderBy(member.age.desc())
                .orderBy(member.username.asc().nullsLast()).fetch();


        //when
        assertThat(result.get(0).getUsername()).isEqualTo("member5");
        assertThat(result.get(1).getUsername()).isEqualTo("member6");
        assertThat(result.get(2).getUsername()).isNull();




    //then
    }
    @Test
    public void paging1(){
    //given
        List<Member> result = queryFactory.selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetch();


        //when
        assertThat(result.get(0).getUsername()).isEqualTo("member3");


    //then


    }
    @Test
    public void paging2(){
    //given
        QueryResults<Member> queryResults = queryFactory.selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetchResults();


        //when
        assertThat(queryResults.getTotal()).isEqualTo(4);
        assertThat(queryResults.getResults().size()).isEqualTo(2);


    //then


    }
    /*
    * 팀의 이름과 각 팀의 평균 연령을 구해라
    * */
    @Test
    public void groupBy(){
    //given
        List<Tuple> result = queryFactory.select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();

        //when
        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);
        //then
        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamA.get(member.age.avg())).isEqualTo(15);

    }

}
