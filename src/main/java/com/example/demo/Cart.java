package com.example.demo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Cart {
    private Integer id;
    private String email;
    private String productId;
    private String color;
    private String size;
    // Getters and setters
}

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
class CartDTO{
	private Integer id;
	private String productId;
    private String name;
    private double price;
    private double discount;
    private String size;
    private String color;
    private int quantity;
    private String image;
    private boolean selected;
}