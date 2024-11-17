package group6.PollingWithHE;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PageController {

    @GetMapping("/create-poll")
    public String createPollPage() {
        // This will look for create_poll.html in src/main/resources/static
        return "create_poll";
    }

    @GetMapping("/poll-questions")
    public String pollQuestionsPage(@RequestParam Long pollId, Model model) {
        // Add the pollId to the model so it can be used in the page
        model.addAttribute("pollId", pollId);
        // This will look for poll_questions.html in src/main/resources/static
        return "question";
    }
}
