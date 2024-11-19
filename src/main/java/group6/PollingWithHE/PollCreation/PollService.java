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
        System.out.println("Call Node.js at :" + url);
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

    public int decryptValue(DecryptRequest decryptRequest) {
        String url = encryptionServiceBaseUrl + "/decrypt";
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("ciphertext", decryptRequest.getCiphertext());

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);

        System.out.println("Plaintext is :");

        System.out.println(response.getBody().get("plaintext"));

        if (response.getStatusCode() == HttpStatus.OK) {
            Object plaintextObj = response.getBody().get("plaintext");
            Integer result = null;
            if (plaintextObj instanceof Integer) {
                // Already an Integer
                result = (Integer) plaintextObj;
            } else if (plaintextObj instanceof String) {
                // Convert from String to Integer
                try {
                    result = Integer.valueOf((String) plaintextObj);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid number format: " + plaintextObj);
                }
            }
            return result.intValue();
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
            try {
                // Parse the JSON response to extract the "ciphertext" field
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(response.getBody());
                return rootNode.get("updated_ciphertext").asText(); // Extract "ciphertext"
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse the response: " + response.getBody(), e);
            }
        } else {
            throw new RuntimeException("Encryption failed: " + response.getBody());
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
        String encrypedOptionId = request.getOptionId();
        DecryptRequest decryptRequest = new DecryptRequest(encrypedOptionId);
        int plainOptionId = decryptValue(decryptRequest);
        Optional<Result> existingResult = resultRepository.findByPollIdAndQuestionIdAndOptionId(request.getPollId(), request.getQuestionId(), plainOptionId);
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
            updatedEncryptedResult = encryptValue(new EncryptRequest(1L));

            Result newResult = new Result();
            newResult.setPollId(request.getPollId());
            newResult.setQuestionId(request.getQuestionId());
            newResult.setOptionId(plainOptionId);
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

    public List<QuestionResultResponse> getPollResults(Integer pollId) {
        // 获取指定投票的所有问题
        List<Question> questions = questionRepository.findByPollIdOrderByQuestionOrderAsc(pollId);

        if (questions.isEmpty()) {
            return List.of();
        }

        return questions.stream().map(question -> {
            // 获取当前问题的所有选项
            List<PollOption> options = optionRepository.findByQuestionIdOrderByOptionOrderAsc(question.getId());

            // 构造每个选项的解密结果
            List<OptionResult> optionResults = options.stream().map(option -> {
                Optional<Result> resultOptional = resultRepository.findByPollIdAndQuestionIdAndOptionId(
                        pollId, question.getId(), option.getId()
                );
                long decryptedResult = 0;

                if (resultOptional.isPresent()) {
                    Result result = resultOptional.get();
                    // 解密加密结果
                    decryptedResult = decryptValue(new DecryptRequest(result.getEncryptedResult()));
                }

                return new OptionResult(option.getId(), option.getOptionText(), decryptedResult);
            }).collect(Collectors.toList());

            return new QuestionResultResponse(question.getId(), question.getTitle(), optionResults);
        }).collect(Collectors.toList());
    }
}

