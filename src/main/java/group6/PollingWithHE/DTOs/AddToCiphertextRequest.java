package group6.PollingWithHE.DTOs;
import lombok.Data;
@Data
public class AddToCiphertextRequest {
    private String ciphertext;
    private Long value;
}
