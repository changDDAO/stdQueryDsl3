package com.changddao.demo.repository;

import com.changddao.demo.dto.MemberSearchCondition;
import com.changddao.demo.dto.MemberTeamDto;
import com.changddao.demo.dto.QMemberTeamDto;
import com.changddao.demo.entity.QMember;
import com.changddao.demo.entity.QTeam;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.changddao.demo.entity.QMember.*;
import static com.changddao.demo.entity.QTeam.*;
import static org.springframework.util.StringUtils.*;

@Repository
public class MemberJpaRepository {
    private JPAQueryFactory queryFactory;

    public MemberJpaRepository(EntityManager em){
        this.queryFactory = new JPAQueryFactory(em);
    }


    public List<MemberTeamDto> searchbyBuilder(MemberSearchCondition condition) {
        BooleanBuilder builder = new BooleanBuilder();
        if (hasText(condition.getUsername())) {
            builder.and(member.username.eq(condition.getUsername()));
        }
        if (hasText(condition.getTeamName())) {
            builder.and(team.name.eq(condition.getTeamName()));
        }
        if (condition.getAgeGoe() != null) {
            builder.and(member.age.goe(condition.getAgeGoe()));
        }
        if (condition.getAgeLoe() != null) {
            builder.and(member.age.loe(condition.getAgeLoe()));
        }
        return queryFactory.select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(builder)
                .fetch();

    }

}
