package group6.PollingWithHE.DTOs;
import java.time.LocalDateTime;
import java.util.List;

public class PollDetailsResponse {
    private Integer id;
    private String pollTitle;
    private LocalDateTime endTime;
    private List<QuestionDTO> questions;

    public PollDetailsResponse(Integer id, String pollTitle, LocalDateTime endTime, List<QuestionDTO> questions) {
        this.id = id;
        this.pollTitle = pollTitle;
        this.endTime = endTime;
        this.questions = questions;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPollTitle() {
        return pollTitle;
    }

    public void setPollTitle(String pollTitle) {
        this.pollTitle = pollTitle;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public List<QuestionDTO> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionDTO> questions) {
        this.questions = questions;
    }
}