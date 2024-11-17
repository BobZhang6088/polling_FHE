package group6.PollingWithHE.Question;

import group6.PollingWithHE.Entities.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Integer> {
    List<Question> findByPollIdOrderByQuestionOrderAsc(Integer pollId);
}