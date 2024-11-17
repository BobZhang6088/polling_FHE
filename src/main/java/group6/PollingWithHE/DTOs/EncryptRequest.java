package group6.PollingWithHE.DTOs;

import lombok.Data;
@Data
public class EncryptRequest {
    private String publicKey;
    private Long value;

    public EncryptRequest(String publicKey, Long value) {
        this.publicKey = publicKey;
        this.value = value;
    }
}
