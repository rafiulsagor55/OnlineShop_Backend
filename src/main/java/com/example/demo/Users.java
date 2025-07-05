package com.example.demo;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Users {
	private String email;
	private String name;
    private byte[] imageData;
    private Timestamp createdAt;
    private String imagePath;
    private String contentType;
}

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
class UsersDTO {
	private String email;
	private String name;
    private String imageData;
    private Timestamp createdAt;
    private String imagePath;
    private String contentType;
}

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
class passwords{
	private String email;
	private String password;
	private int count;
}



@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
class codes{
	private String email;
	private int code;
	private int count;
	private Timestamp createdAt;
}



