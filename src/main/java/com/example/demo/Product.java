package com.example.demo;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

	    private String id;
	    private String name;
	    private String description;
	    private String type;
	    private String brand;
	    private String material;
	    private String category;
	    private String availability;
	    private double price;
	    private double discount;
	    private double rating;
	    private List<String> sizes;
	    private String sizeDetails;
	    private Map<String, List<String>> colors; // Color -> List of base64-encoded image strings
	    private String deliveryInfo;
	    private String returnPolicy;
	    private String trustInfo;
	    private String gender;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class ProductDTO{
	private String id;
    private String name;
    private String description;
    private String type;
    private String brand;
    private String material;
    private String category;
    private String availability;
    private double price;
    private double discount;
    private double rating;
    private List<String> size;
    private List<String> color;
    private String imageData;
    private String deliveryInfo;
    private String returnPolicy;
    private String trustInfo;
    private String gender;
}
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class ColorImagePair {
    private String color;
    private byte[] imageData;
    private String contentType;

}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class ObjectForCart {
	private String name;
	private double price;
    private double discount;

}





