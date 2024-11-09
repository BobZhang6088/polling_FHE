package group6.PollingWithHE.Question;

import org.springframework.data.jpa.repository.JpaRepository;

import group6.PollingWithHE.Entities.PollOption;

public interface OptionRepository extends JpaRepository<PollOption, Integer> {

}