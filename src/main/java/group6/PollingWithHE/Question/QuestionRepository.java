package group6.PollingWithHE.Question;

import group6.PollingWithHE.Entities.Question;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, Integer> {
}