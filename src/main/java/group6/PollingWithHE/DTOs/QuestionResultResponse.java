package group6.PollingWithHE.DTOs;

import java.util.List;

public class QuestionResultResponse {
    private Integer questionId;
    private String questionTitle;
    private List<OptionResult> options;

    public QuestionResultResponse(Integer questionId, String questionTitle, List<OptionResult> options) {
        this.questionId = questionId;
        this.questionTitle = questionTitle;
        this.options = options;
    }

    public Integer getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Integer questionId) {
        this.questionId = questionId;
    }

    public String getQuestionTitle() {
        return questionTitle;
    }

    public void setQuestionTitle(String questionTitle) {
        this.questionTitle = questionTitle;
    }

    public List<OptionResult> getOptions() {
        return options;
    }

    public void setOptions(List<OptionResult> options) {
        this.options = options;
    }
}
