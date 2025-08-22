package com.example.demo;

import java.sql.Timestamp;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Order {
	private String UUID;
	private Long serialId;
	private String id;
    private Timestamp date;
    private String status;
    private String customer;
    private String email;
    private String contact;
    private String address;
    private String deliveryMethod;
    private String paymentMethod;
    private String payment;
    private Timestamp processedDate;
    private Timestamp shippedDate;
    private Timestamp deliveredDate;
    private Timestamp readyDate;
    private Timestamp pickedDate;
    private Timestamp cancelledDate;
    private List<Item> items;
    private double total;

}

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
class updateOrder {
	private Long serialId;
    private String status;
    private String payment;
    private Timestamp processedDate;
    private Timestamp shippedDate;
    private Timestamp deliveredDate;
    private Timestamp readyDate;
    private Timestamp pickedDate;
    private Timestamp cancelledDate;

}

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
class Item{
	private Long item_id;
	private String order_UUID;
	private String productId;
    private String name;
    private String color;
    private String size;
    private double price;
    private int quantity;
}
