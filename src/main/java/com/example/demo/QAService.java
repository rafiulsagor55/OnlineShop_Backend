package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QAService {

    @Autowired
    private QARepository qaRepository;

    @Autowired
    private UserService userService; // To validate user

    public List<Question> getQuestionsForProduct(String productId, String sortBy, String filter, String jwt, String ip, String userAgent) {
        String currentEmail = userService.getEmailFromToken(jwt, ip, userAgent);
        List<Question> questions = qaRepository.getQuestionsForProduct(productId, sortBy, filter, currentEmail != null ? currentEmail : "");
        for (Question q : questions) {
            q.setAnswers(qaRepository.getAnswersForQuestion(q.getId(), currentEmail != null ? currentEmail : ""));
        }
        return questions;
    }

    public Question addQuestion(String productId, String question, String jwt, String ip, String userAgent) {
        String email = validateUser(jwt, ip, userAgent);
        Long id = qaRepository.insertQuestion(productId, email, question);
        // Fetch the new question
        return getQuestionsForProduct(productId, "recent", "all", jwt, ip, userAgent).stream()
                .filter(q -> q.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Failed to add question"));
    }

    public boolean deleteQuestion(Long questionId, String productId, String jwt, String ip, String userAgent) {
        String email = validateUser(jwt, ip, userAgent);
        if (!qaRepository.questionExistsForProduct(questionId, productId)) {
            throw new IllegalArgumentException("Question not found or does not belong to product");
        }
        return qaRepository.deleteQuestion(questionId, email);
    }

    public Answer addAnswer(Long questionId, String answer, String jwt, String ip, String userAgent) {
        String email = validateUser(jwt, ip, userAgent);
        Long id = qaRepository.insertAnswer(questionId, email, answer);
        // Fetch the new answer
        List<Answer> answers = qaRepository.getAnswersForQuestion(questionId, email);
        return answers.stream()
                .filter(a -> a.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Failed to add answer"));
    }

    public boolean deleteAnswer(Long answerId, String jwt, String ip, String userAgent) {
        String email = validateUser(jwt, ip, userAgent);
        return qaRepository.deleteAnswer(answerId, email);
    }

    public void vote(String entityType, Long entityId, String voteType, String jwt, String ip, String userAgent) {
        String email = validateUser(jwt, ip, userAgent);
        if (!"like".equals(voteType) && !"dislike".equals(voteType)) {
            throw new IllegalArgumentException("Invalid vote type");
        }
        if (!"question".equals(entityType) && !"answer".equals(entityType)) {
            throw new IllegalArgumentException("Invalid entity type");
        }
        qaRepository.upsertVote(entityType, entityId, email, voteType);
    }

    private String validateUser(String jwt, String ip, String userAgent) {
        if (!userService.checkTokenValidity(jwt, ip, userAgent)) {
            throw new IllegalArgumentException("Unauthorized");
        }
        String email = userService.getEmailFromToken(jwt, ip, userAgent);
        if (email == null) {
            throw new IllegalArgumentException("Unauthorized");
        }
        return email;
    }
}