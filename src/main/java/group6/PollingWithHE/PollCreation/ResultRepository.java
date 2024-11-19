package group6.PollingWithHE.PollCreation;
import group6.PollingWithHE.Entities.Result;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResultRepository extends JpaRepository<Result, Integer> {
    Optional<Result> findByPollIdAndQuestionIdAndOptionId(int pollId, int questionId, int optionId);
    Optional<Result> findByPollIdAndQuestionId(int pollId, int questionId);
}
