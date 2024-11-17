package group6.PollingWithHE.DTOs;
import lombok.Data;
@Data
public class DecryptRequest {
    private String secretKey;
    private String ciphertext;
}
