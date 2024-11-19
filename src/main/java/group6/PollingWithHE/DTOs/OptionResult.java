package group6.PollingWithHE.DTOs;

public class OptionResult {
    private Integer optionId;
    private String optionText;
    private long votes;

    public OptionResult(Integer optionId, String optionText, long votes) {
        this.optionId = optionId;
        this.optionText = optionText;
        this.votes = votes;
    }

    public Integer getOptionId() {
        return optionId;
    }

    public void setOptionId(Integer optionId) {
        this.optionId = optionId;
    }

    public String getOptionText() {
        return optionText;
    }

    public void setOptionText(String optionText) {
        this.optionText = optionText;
    }

    public long getVotes() {
        return votes;
    }

    public void setVotes(long votes) {
        this.votes = votes;
    }
}
