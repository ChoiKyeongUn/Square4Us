package com.ssafy.square4us.api.mvc.service;

import com.ssafy.square4us.api.mvc.model.entity.Member;
import com.ssafy.square4us.api.mvc.model.entity.Study;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface StudyService {
    Study createStudy(Study.CreatePostReq studyInfo, Member member);

    Study findByStudyId(Long studyId);

    List<Study> findAllStudies();

    @Transactional
    boolean deleteByStudyId(String email, Long studyId);
}
