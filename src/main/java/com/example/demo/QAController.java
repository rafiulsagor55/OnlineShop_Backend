package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RequestMapping("/api/products/{productId}")
public class QAController {

    @Autowired
    private QAService qaService;

    @GetMapping("/questions")
    public ResponseEntity<List<Question>> getQuestions(
            @PathVariable String productId,
            @RequestParam(defaultValue = "recent") String sort,
            @RequestParam(defaultValue = "all") String filter,
            @CookieValue(name = "token", required = false) String jwt,
            HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null) ip = request.getRemoteAddr();
        List<Question> questions = qaService.getQuestionsForProduct(productId, sort, filter, jwt, ip, userAgent);
        return ResponseEntity.ok(questions);
    }

    @PostMapping("/questions")
    public ResponseEntity<Question> addQuestion(
            @PathVariable String productId,
            @RequestBody Map<String, String> body,
            @CookieValue(name = "token", required = false) String jwt,
            HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null) ip = request.getRemoteAddr();
        String question = body.get("question");
        if (question == null || question.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        Question newQuestion = qaService.addQuestion(productId, question, jwt, ip, userAgent);
        return ResponseEntity.ok(newQuestion);
    }

    @DeleteMapping("/questions/{questionId}")
    public ResponseEntity<Void> deleteQuestion(
            @PathVariable String productId,
            @PathVariable Long questionId,
            @CookieValue(name = "token", required = false) String jwt,
            HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null) ip = request.getRemoteAddr();
        boolean deleted = qaService.deleteQuestion(questionId, productId, jwt, ip, userAgent);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @PostMapping("/questions/{questionId}/answers")
    public ResponseEntity<Answer> addAnswer(
            @PathVariable String productId,
            @PathVariable Long questionId,
            @RequestBody Map<String, String> body,
            @CookieValue(name = "token", required = false) String jwt,
            HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null) ip = request.getRemoteAddr();
        String answer = body.get("answer");
        if (answer == null || answer.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        Answer newAnswer = qaService.addAnswer(questionId, answer, jwt, ip, userAgent);
        return ResponseEntity.ok(newAnswer);
    }

    @DeleteMapping("/answers/{answerId}")
    public ResponseEntity<Void> deleteAnswer(
            @PathVariable String productId, // Not used but for routing consistency
            @PathVariable Long answerId,
            @CookieValue(name = "token", required = false) String jwt,
            HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null) ip = request.getRemoteAddr();
        boolean deleted = qaService.deleteAnswer(answerId, jwt, ip, userAgent);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @PostMapping("/vote")
    public ResponseEntity<Void> vote(
            @PathVariable String productId, // Not used but for routing
            @RequestBody Map<String, Object> body,
            @CookieValue(name = "token", required = false) String jwt,
            HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null) ip = request.getRemoteAddr();
        String entityType = (String) body.get("entityType");
        Long entityId = Long.valueOf(body.get("entityId").toString());
        String voteType = (String) body.get("voteType");
        qaService.vote(entityType, entityId, voteType, jwt, ip, userAgent);
        return ResponseEntity.ok().build();
    }
}