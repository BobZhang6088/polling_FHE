package group6.PollingWithHE.DTOs;

import lombok.Data;
@Data
public class EncryptRequest {
    private Long value;

    public EncryptRequest(Long value) {
        this.value = value;
    }
}
