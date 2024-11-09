package group6.PollingWithHE.DTOs;

import group6.PollingWithHE.Entities.Poll;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PollResponse {
    private int id;
    private String title;
    private LocalDateTime endTime;

    public PollResponse(Poll poll) {
        this.id = poll.getId();
        this.title = poll.getTitle();
        this.endTime = poll.getEndTime();
    }
}
