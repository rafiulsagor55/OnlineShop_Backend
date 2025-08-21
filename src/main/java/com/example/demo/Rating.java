package com.example.demo;

import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;

@Data
@Builder
public class Rating {
    private Long id;
    private String productId;
    private String userEmail;
    private int rating;
    private Timestamp createdAt;
}