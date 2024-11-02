package group6.PollingWithHE.PollCreation;

import group6.PollingWithHE.Entities.Poll;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PollRepository extends JpaRepository<Poll, Integer> {
}