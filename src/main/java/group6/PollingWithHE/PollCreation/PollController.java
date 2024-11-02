package group6.PollingWithHE.PollCreation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import group6.PollingWithHE.DTOs.*;
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
}