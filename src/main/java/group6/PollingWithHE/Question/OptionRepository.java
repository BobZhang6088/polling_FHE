package group6.PollingWithHE.Question;

import group6.PollingWithHE.DTOs.OptionDTO;
import org.springframework.data.jpa.repository.JpaRepository;

import group6.PollingWithHE.Entities.PollOption;

import java.util.List;

public interface OptionRepository extends JpaRepository<PollOption, Integer> {
    List<PollOption> findByQuestionIdOrderByOptionOrderAsc(Integer questionId);
}