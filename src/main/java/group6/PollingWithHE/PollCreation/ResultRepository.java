package group6.PollingWithHE.PollCreation;
import group6.PollingWithHE.Entities.Result;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResultRepository extends JpaRepository<Result, Integer> {
    Optional<Result> findByPollIdAndQuestionIdAndEncryptedOptionId(int pollId, int questionId, String encryptedOptionId);
    Optional<Result> findByPollIdAndQuestionId(int pollId, int questionId);
}
