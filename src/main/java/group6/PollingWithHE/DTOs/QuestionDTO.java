package group6.PollingWithHE.DTOs;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;


@Data
public class QuestionDTO {
    private String title;
    private int order;
    private List<OptionDTO> options;
}
