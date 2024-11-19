package group6.PollingWithHE.PollCreation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import group6.PollingWithHE.DTOs.*;

import java.util.List;

@RestController
@RequestMapping("/api/polls")
public class PollController {

    @Autowired
    private PollService pollService;

    @PostMapping("/create")
    public ResponseEntity<String> createPoll(@RequestBody PollDTO pollDTO) {
        try {
            pollService.createPoll(pollDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body("Poll created successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating poll");
        }
    }

    @GetMapping("/ongoing")
    public ResponseEntity<List<PollResponse>> getOngoingPolls() {
        List<PollResponse> ongoingPolls = pollService.getOngoingPolls();
        return ResponseEntity.ok(ongoingPolls);
    }

    @GetMapping("/completed")
    public ResponseEntity<List<PollResponse>> getCompletedPolls() {
        List<PollResponse> completedPolls = pollService.getCompletedPolls();
        return ResponseEntity.ok(completedPolls);
    }

    @PostMapping("/encrypt")
    public ResponseEntity<String> encryptValue(@RequestBody EncryptRequest encryptRequest) {
        String encryptedValue = pollService.encryptValue(encryptRequest);
        return ResponseEntity.ok(encryptedValue);
    }

    @PostMapping("/decrypt")
    public ResponseEntity<Long> decryptValue(@RequestBody DecryptRequest decryptRequest) {
        int decryptedValue = pollService.decryptValue(decryptRequest);
        return ResponseEntity.ok(Long.valueOf(decryptedValue));
    }

    @GetMapping("/get_secret_key")
    public ResponseEntity<String> getSecretKey() {
        String secretKey = pollService.getSecretKey();
        return ResponseEntity.ok(secretKey);
    }

    @GetMapping("/get_public_key")
    public ResponseEntity<String> getPublicKey() {
        System.out.println("Call get_public_key");
        String publicKey = pollService.getPublicKey();
        return ResponseEntity.ok(publicKey);
    }

    @PostMapping("/add_to_ciphertext")
    public ResponseEntity<String> addToCiphertext(@RequestBody AddToCiphertextRequest addToCiphertextRequest) {
        String updatedCiphertext = pollService.addToCiphertext(addToCiphertextRequest);
        return ResponseEntity.ok(updatedCiphertext);
    }

    @PostMapping("/store_encrypted_result")
    public ResponseEntity<String> storeEncryptedResult(@RequestBody StoreEncryptedResultRequest request) {
        System.out.println("calling store_encrypted_result from controller");
        pollService.storeEncryptedResult(request);
        return ResponseEntity.ok("Encrypted result stored successfully");
    }


    @GetMapping("/{pollId}")
    public ResponseEntity<PollDetailsResponse> getPollDetails(@PathVariable Integer pollId) {
        PollDetailsResponse response = pollService.getPollDetails(pollId);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }


    @GetMapping("/get_encryption_parameters")
    public ResponseEntity<String> getEncryptionParameters() {
        System.out.println("Call get_encryption_parameters");
        String encryptionParameters = pollService.getEncryptionParameters();
        return ResponseEntity.ok(encryptionParameters);
    }

    @GetMapping("/{pollId}/results")
    public ResponseEntity<List<QuestionResultResponse>> getPollResults(@PathVariable Integer pollId) {
        List<QuestionResultResponse> results = pollService.getPollResults(pollId);
        if (results.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(results);
    }


}