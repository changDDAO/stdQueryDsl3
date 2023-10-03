package com.changddao.demo.repository;

import com.changddao.demo.dto.MemberSearchCondition;
import com.changddao.demo.dto.MemberTeamDto;

import java.util.List;

public interface MemberRepositoryCustom {
    List<MemberTeamDto> search(MemberSearchCondition condition);
}
