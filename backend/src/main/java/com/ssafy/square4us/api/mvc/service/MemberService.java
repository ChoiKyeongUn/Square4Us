package com.ssafy.square4us.api.mvc.service;

import com.ssafy.square4us.api.mvc.model.entity.Member;
import com.ssafy.square4us.api.request.MemberJoinPostReq;
import com.ssafy.square4us.api.request.MemberUpdatePatchReq;

public interface MemberService {
    Member getMemberByEmail(String email);

    Member createMember(MemberJoinPostReq joinInfo);

    Long updateMemberByEmail(String email, MemberUpdatePatchReq updateInfo);
}
