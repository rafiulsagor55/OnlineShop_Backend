package com.example.demo;

import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;

@Data
@Builder
public class ReviewVote {
    private Long id;
    private Long reviewId;
    private String userEmail;
    private String voteType;
    private Timestamp createdAt;
}