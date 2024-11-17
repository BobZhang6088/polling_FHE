package group6.PollingWithHE.DTOs;
import lombok.Data;
@Data
public class StoreEncryptedResultRequest {
    private int pollId;
    private int questionId;
    private String optionId;
    private String publicKey;
}
