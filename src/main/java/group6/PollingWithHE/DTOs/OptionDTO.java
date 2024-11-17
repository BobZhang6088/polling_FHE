package group6.PollingWithHE.DTOs;

import lombok.Data;


@Data
public class OptionDTO {
    private Integer id;
    private String text;
    private int order;

    public OptionDTO(Integer id, String text, int order) {
        this.id = id;
        this.text = text;
        this.order = order;
    }
}