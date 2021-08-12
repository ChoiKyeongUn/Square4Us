package com.ssafy.square4us.api.mvc.service;

import com.ssafy.square4us.api.mvc.model.dto.MeetingDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface MeetingService {
    MeetingDTO createMeeting(Long studyId, int maximum, MultipartFile thumbnail) throws IOException;

    MeetingDTO enterMeeting(Long meetingId);

    List<MeetingDTO> findAllMeetings();

    List<MeetingDTO> findMeetingsByStudy(Long studyId);

    void updateThumbnail(Long meetingId, MultipartFile thumbnail) throws IOException;

    void deleteThumbnailById(Long meetingId);
}
