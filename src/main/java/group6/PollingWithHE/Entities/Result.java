package group6.PollingWithHE.Entities;
import jakarta.persistence.*;

@Entity
public class Result {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private int pollId;
    private int questionId;
    private int optionId;
    @Column(columnDefinition = "LONGTEXT")
    private String encryptedResult;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPollId() {
        return pollId;
    }

    public void setPollId(int pollId) {
        this.pollId = pollId;
    }

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public int getOptionId() {
        return optionId;
    }

    public void setOptionId(int optionId) {
        this.optionId = optionId;
    }

    public String getEncryptedResult() {
        return encryptedResult;
    }

    public void setEncryptedResult(String encryptedResult) {
        this.encryptedResult = encryptedResult;
    }
}
