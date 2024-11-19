package group6.PollingWithHE.DTOs;
import lombok.Data;
@Data
public class DecryptRequest {
    private String ciphertext;

    public DecryptRequest(String ciphertext) {
        this.ciphertext = ciphertext;
    }
}
