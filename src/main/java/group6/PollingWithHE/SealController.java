package group6.PollingWithHE;
import group6.PollingWithHE.Encryption.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@RestController
public class SealController {

    @Autowired
    private SEALService sealService;

    // Endpoint to generate a new secret key
    @GetMapping("/generateSecretKey")
    public String generateSecretKey() {
        return Arrays.toString(sealService.generatePrivateKey());
    }

//    // Endpoint to generate a public key using a provided secret key
//    @GetMapping("/generatePublicKey")
//    public String generatePublicKey(@RequestParam String secretKey) {
//        return sealService.generatePublicKey(secretKey);
//    }
//
//    // Endpoint to encrypt an integer using a provided public key
//    @GetMapping("/encryptInteger")
//    public String encryptInteger(@RequestParam int value, @RequestParam String publicKey) {
//        return sealService.encryptInteger(value, publicKey);
//    }
//
//    // Endpoint to add two encrypted integers (ciphertexts)
//    @GetMapping("/addEncryptedIntegers")
//    public String addEncryptedIntegers(@RequestParam String ciphertext1, @RequestParam String ciphertext2) {
//        return sealService.addEncryptedIntegers(ciphertext1, ciphertext2);
//    }
}