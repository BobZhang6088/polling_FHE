package group6.PollingWithHE.DTOs;


import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PollDTO {
    private String title;
    private LocalDateTime endTime;
    private List<QuestionDTO> questions;
}
