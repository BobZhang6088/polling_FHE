package group6.PollingWithHE.PollCreation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import group6.PollingWithHE.Question.OptionRepository;
import group6.PollingWithHE.Question.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import group6.PollingWithHE.DTOs.*;
import group6.PollingWithHE.Entities.*;

import jakarta.transaction.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PollService {

    @Autowired
    private PollRepository pollRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private OptionRepository optionRepository;


    @Autowired
    private ResultRepository resultRepository;

    private final String encryptionServiceBaseUrl = "http://node-seal-service:3000";
    private final RestTemplate restTemplate = new RestTemplate();


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

    public String getSecretKey() {
        String url = encryptionServiceBaseUrl + "/get_secret_key";
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, null, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            throw new RuntimeException("Failed to get secret key: " + response.getBody());
        }
    }

    public String getPublicKey() {
        String url = encryptionServiceBaseUrl + "/get_public_key";
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        Map<String, Object> requestBody = new HashMap<>();

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            throw new RuntimeException("Failed to get public key: " + response.getBody());
        }
    }

    public String encryptValue(EncryptRequest encryptRequest) {
        String url = encryptionServiceBaseUrl + "/encrypt_with_public_key";
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("public_key", encryptRequest.getPublicKey());
        requestBody.put("value", encryptRequest.getValue());

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            try {
                // Parse the JSON response to extract the "ciphertext" field
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(response.getBody());
                return rootNode.get("ciphertext").asText(); // Extract "ciphertext"
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse the response: " + response.getBody(), e);
            }
        } else {
            throw new RuntimeException("Encryption failed: " + response.getBody());
        }
    }

    public Long decryptValue(DecryptRequest decryptRequest) {
        String url = encryptionServiceBaseUrl + "/decrypt";
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("secret_key", decryptRequest.getSecretKey());
        requestBody.put("ciphertext", decryptRequest.getCiphertext());

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return Long.valueOf(response.getBody().get("plaintext").toString());
        } else {
            throw new RuntimeException("Decryption failed: " + response.getBody());
        }
    }

    public String addToCiphertext(AddToCiphertextRequest addToCiphertextRequest) {
        String url = encryptionServiceBaseUrl + "/add_to_ciphertext";
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("ciphertext", addToCiphertextRequest.getCiphertext());
        requestBody.put("value", addToCiphertextRequest.getValue());

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            throw new RuntimeException("Failed to add to ciphertext: " + response.getBody());
        }
    }

    public String getEncryptionParameters() {
        String url = encryptionServiceBaseUrl + "/get_encryption_parameters";
        System.out.println("Call c++ at :" + url);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, null, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            System.out.println("wrong--" + response.getBody());
            throw new RuntimeException("Failed to get encryption parameters: " + response.getBody());
        }
    }

    public void storeEncryptedResult(StoreEncryptedResultRequest request) {
        Optional<Result> existingResult = resultRepository.findByPollIdAndQuestionIdAndEncryptedOptionId(request.getPollId(), request.getQuestionId(), request.getOptionId());
        String updatedEncryptedResult;
        System.out.println("calling storeEncryptedResult from Service");
        if (existingResult.isPresent()) {
            System.out.println("The result exists");
            // If result exists, add one to the encrypted result using C++ service
            AddToCiphertextRequest addToCiphertextRequest = new AddToCiphertextRequest();
            addToCiphertextRequest.setCiphertext(existingResult.get().getEncryptedResult());
            addToCiphertextRequest.setValue(1L);
            updatedEncryptedResult = addToCiphertext(addToCiphertextRequest);

            // Update the existing result
            existingResult.get().setEncryptedResult(updatedEncryptedResult);
            resultRepository.save(existingResult.get());
        } else {
            System.out.println("The result does not exist");
            // If result does not exist, create a new result
            updatedEncryptedResult = encryptValue(new EncryptRequest(request.getPublicKey(), 1L));

            System.out.println("op id = " + request.getOptionId());

            Result newResult = new Result();
            newResult.setPollId(request.getPollId());
            newResult.setQuestionId(request.getQuestionId());
            newResult.setEncryptedOptionId(request.getOptionId());
            newResult.setEncryptedResult(updatedEncryptedResult);
            resultRepository.save(newResult);
        }
    }

    public PollDetailsResponse getPollDetails(Integer pollId) {
        Optional<Poll> pollOptional = pollRepository.findById(pollId);
        if (!pollOptional.isPresent()) {
            return null;
        }

        Poll poll = pollOptional.get();

        List<Question> questionList = questionRepository.findByPollIdOrderByQuestionOrderAsc(pollId);
        List<QuestionDTO> questionDTOList = questionList.stream().map(q -> {
            List<OptionDTO> optionDTOList = optionRepository.findByQuestionIdOrderByOptionOrderAsc(q.getId())
                    .stream().map(o -> new OptionDTO(o.getId(), o.getOptionText(), o.getOptionOrder())).collect(Collectors.toList());
            return new QuestionDTO(q.getId(),q.getTitle(), q.getQuestionOrder(), optionDTOList);
        }).collect(Collectors.toList());



        return new PollDetailsResponse(poll.getId(), poll.getTitle(), poll.getEndTime(), questionDTOList);
    }
}

