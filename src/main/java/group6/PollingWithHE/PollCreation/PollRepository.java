package group6.PollingWithHE.PollCreation;

import group6.PollingWithHE.Entities.Poll;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PollRepository extends JpaRepository<Poll, Integer> {
    List<Poll> findByEndTimeAfter(LocalDateTime endTime);
    List<Poll> findByEndTimeBefore(LocalDateTime endTime);
}