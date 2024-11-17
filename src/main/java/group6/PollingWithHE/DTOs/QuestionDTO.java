package group6.PollingWithHE.DTOs;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;


@Data
public class QuestionDTO {
    private Integer id;
    private String title;
    private int order;
    private List<OptionDTO> options;

    public QuestionDTO(Integer id, String title, int order, List<OptionDTO> options) {
        this.id = id;
        this.title = title;
        this.order = order;
        this.options = options;
    }
}
