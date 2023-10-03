package com.changddao.demo;

import com.changddao.demo.dto.MemberDto;
import com.changddao.demo.dto.QMemberDto;
import com.changddao.demo.dto.UserDto;
import com.changddao.demo.entity.Member;
import com.changddao.demo.entity.QMember;
import com.changddao.demo.entity.QTeam;
import com.changddao.demo.entity.Team;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.*;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
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

    @PersistenceUnit
    EntityManagerFactory emf;
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
    /*
    * 팀A에 소속된 모든 회원을 조회
    * */
    @Test
    public void join(){
    //given
        List<Member> aMembers = queryFactory.select(member)
                .from(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();


        //when
    assertThat(aMembers).extracting("username")
            .containsExactly("member1", "member2");



    //then


    }
    @Test
    public void fetchJoinNo(){
    //given
    em.flush();
    em.clear();

        Member findMember = queryFactory.selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();


        //when
        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("페치조인 미적용").isFalse();


        //then


    }
    @Test
    public void fetchJoin(){
    //given
        Member findMember = queryFactory.selectFrom(member)
                .join(member.team, team).fetchJoin()
                .where(member.username.eq("member1"))
                .fetchOne();


        //when
        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());


        //then
        assertThat(loaded).isTrue();

    }

    /*
    * 나이가 가장많은 회원 조회
    * */
    @Test
    public void subQuery(){
    //given
        QMember memberSub = new QMember("memberSub");
        List<Member> result = queryFactory.selectFrom(member)
                .where(member.age.eq(
                        JPAExpressions.select(memberSub.age.max())
                                .from(memberSub)

                )).fetch();


        //when
        int age = result.get(0).getAge();
        //then
    assertThat(age).isEqualTo(40);

    }

    /*
    * 나이가 평균 이상인 회원 조회
    * */
    @Test
    public void subQuery2(){
        QMember subMember = new QMember("subMember");
    //given
        List<Member> result = queryFactory.selectFrom(member)
                .where(member.age.goe(
                        JPAExpressions.select(subMember.age.avg())
                                .from(subMember)

                )).fetch();


        //when
        for (Member member1 : result) {
            System.out.println(member1);
        }


    //then


    }
    //case when절
    @Test
    public void complexCase(){
    //given
        List<String> result = queryFactory.select(new CaseBuilder()
                        .when(member.age.between(10, 20)).then("10~20살")
                        .when(member.age.between(21, 30)).then("21~30살")
                        .otherwise("기타")
                ).from(member)
                .fetch();


        //when
        for (String s : result) {
            System.out.println("s = " + s);
        }


    //then


    }
    /*dto로 조회하기*/
    @Test
    public void jpqlDto(){
    //given
        List<MemberDto> result = em.createQuery("select new com.changddao.demo.dto.MemberDto(m.username, m.age)" +
                "from Member m", MemberDto.class).getResultList();


        //when
        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }


    //then


    }
    @Test
    public void findByConstructor(){
    //given
        List<UserDto> result = queryFactory.select(Projections.constructor(UserDto.class,
                        member.username, member.age))
                .from(member)
                .fetch();


        //when
        for (UserDto userDto : result) {
            System.out.println("userDto = " + userDto);
        }


    //then


    }
    @Test
    public void queryProjection(){
    //given
        List<MemberDto> result = queryFactory.select(new QMemberDto(member.username, member.age))
                .from(member)
                .fetch();


        //when
        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }


    //then
    }
    @Test
    public void dynamicQUery_WhereParam(){
    //given
    String usernameParam ="member1";
    Integer ageParam =null;

    List<Member> result = searchMember2(usernameParam, ageParam);
    //when
        for (Member member1 : result) {
            System.out.println("member1 = " + member1);
        }


    //then


    }
    private List<Member> searchMember2(String usernameCond, Integer ageCond) {
        return queryFactory.selectFrom(member)
                .where(usernameEq(usernameCond), ageEq(ageCond))
                .fetch();
    }

    private BooleanExpression ageEq(Integer ageCond) {
        return ageCond==null? null : member.age.eq(ageCond);
    }

    private BooleanExpression usernameEq(String usernameCond) {
        return usernameCond==null? null: member.username.eq(usernameCond);
    }

    @Test
    @Commit
    public void bulkUpdate(){
    //given
        long count = queryFactory.update(member)
                .set(member.username,"비회원")
                .where(member.age.lt(28))
                .execute();

        em.flush();
        em.clear();


        //when
        List<Member> result = queryFactory.selectFrom(member)
                .fetch();

        for (Member member1 : result) {
            System.out.println(member1);
        }

    }



    //then


    }



