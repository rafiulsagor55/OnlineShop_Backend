package com.example.demo;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Notification{
	private String id;
	private String email;
	private String title;
	private String message;
	private Timestamp timestamp;
	private boolean read;
	private String type;
	private long serialId;
	
}

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
class ActivityLog{
	private String id;
	private String title;
	private String message;
	private Timestamp timestamp;
	private String type;
	
}

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
class newItemsCounter{
	private String email;
	private int notifications;
	private int cartItems;
}
