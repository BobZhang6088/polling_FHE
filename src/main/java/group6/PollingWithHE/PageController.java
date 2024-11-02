package group6.PollingWithHE;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/create-poll")
    public String createPollPage() {
        // This will look for create_poll.html in src/main/resources/static
        return "create_poll";
    }
}
