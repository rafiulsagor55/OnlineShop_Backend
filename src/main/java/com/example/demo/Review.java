package com.example.demo;

import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;

@Data
@Builder
public class Review {
    private Long id;
    private String productId;
    private String userEmail;
    private String userName;
    private String reviewText;
    private Timestamp createdAt;
    private int likeCount;
    private int dislikeCount;
    private String userVote;
}