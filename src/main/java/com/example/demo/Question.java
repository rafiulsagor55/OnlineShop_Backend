package com.example.demo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Question {
    private Long id;
    private String productId; // Changed from Long to String
    private String userEmail;
    private String userName;
    private String question;
    private Timestamp createdAt;
    private int likeCount;
    private int dislikeCount;
    private String userVote; // "like", "dislike", or null
    private List<Answer> answers;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class Answer {
    private Long id;
    private Long questionId;
    private String userEmail;
    private String userName;
    private String answer;
    private Timestamp createdAt;
    private int likeCount;
    private int dislikeCount;
    private String userVote; // "like", "dislike", or null
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class Vote {
    private Long id;
    private String entityType; // "question" or "answer"
    private Long entityId;
    private String userEmail;
    private String voteType; // "like" or "dislike"
    private Timestamp createdAt;
}