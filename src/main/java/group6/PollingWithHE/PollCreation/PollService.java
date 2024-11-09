package group6.PollingWithHE.PollCreation;

import group6.PollingWithHE.Question.OptionRepository;
import group6.PollingWithHE.Question.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import group6.PollingWithHE.DTOs.*;
import group6.PollingWithHE.Entities.*;

import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PollService {

    @Autowired
    private PollRepository pollRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private OptionRepository optionRepository;

    @Transactional
    public void createPoll(PollDTO pollDTO) {
        try {
            Poll poll = new Poll();
            poll.setTitle(pollDTO.getTitle());
            poll.setEndTime(pollDTO.getEndTime());
            pollRepository.save(poll);
            System.out.println("Saved Poll ID: " + poll.getId());

            for (QuestionDTO questionDTO : pollDTO.getQuestions()) {
                Question question = new Question();
                question.setPoll(poll);
                question.setTitle(questionDTO.getTitle());
                question.setQuestionOrder(questionDTO.getOrder());
                questionRepository.save(question);
                System.out.println("Saved Question ID: " + question.getId());

                for (OptionDTO optionDTO : questionDTO.getOptions()) {
                    PollOption option = new PollOption();
                    option.setQuestion(question);
                    option.setOptionText(optionDTO.getText());
                    option.setOptionOrder(optionDTO.getOrder());
                    optionRepository.save(option);
                }
            }
        } catch (Exception e) {
            System.err.println("Error creating poll: " + e.getMessage());
            throw new RuntimeException(e);
        }

    }

    public List<PollResponse> getOngoingPolls() {
        LocalDateTime now = LocalDateTime.now();
        List<Poll> ongoingPolls = pollRepository.findByEndTimeAfter(now);
        return ongoingPolls.stream().map(PollResponse::new).collect(Collectors.toList());
    }

    public List<PollResponse> getCompletedPolls() {
        LocalDateTime now = LocalDateTime.now();
        List<Poll> completedPolls = pollRepository.findByEndTimeBefore(now);
        return completedPolls.stream().map(PollResponse::new).collect(Collectors.toList());
    }
}

