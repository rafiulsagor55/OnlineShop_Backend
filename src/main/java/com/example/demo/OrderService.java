package com.example.demo;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

	@Autowired
	OrderRepository orderRepository;
	public void saveOrder(Order order) {
		for (Item item : order.getItems()) {
			System.out.println(orderRepository.isProductExist(item.getProductId(), item.getSize(), item.getColor()));
			if(!orderRepository.productAvailability(item.getProductId())) {
				throw new IllegalArgumentException("Product " + item.getProductId()+ " is not available now!");
			}
			if(!orderRepository.isProductExist(item.getProductId(), item.getSize(), item.getColor())) {
				throw new IllegalArgumentException("Product is not exist!");
			}
		}
		orderRepository.createOrder(order);
	}
	
	public List<Order> getOrdersWithItemsAndProductDetailsByEmail(String email) {
	    // 1. Retrieve all orders by email
	    List<Order> orders = orderRepository.getOrdersByEmail(email);

	    // 2. Retrieve all order items for the given email
	    List<Item> items = orderRepository.getOrderItemsByEmail(email);

	    // 3. Map each item to its corresponding product details
//	    for (Item item : items) {
//	        Product product = orderRepository.getProductInfo(item.getProductId());
//	        item.setName(product.getName());
//	        item.setPrice(product.getPrice()-(product.getPrice()*(product.getDiscount()/100)));
//	    }

	    // 4. Associate items with the corresponding order and calculate the total
	    for (Order order : orders) {
	        List<Item> orderItems = items.stream()
	                .filter(item -> item.getOrder_UUID().equals(order.getUUID()))
	                .collect(Collectors.toList());
	        
	        // Setting items in the order
	        order.setItems(orderItems);
	        order.setId("ORD-0" + order.getSerialId());
	        
	        // Calculate total price for the order
	        double total = 0.0;
	        for (Item item : orderItems) {
	            total += (item.getPrice()) * item.getQuantity();
	        }
	        order.setTotal(total);
	    }

	    return orders.stream().sorted(Comparator.comparing(Order::getSerialId).reversed()).collect(Collectors.toList());
	}
	
	public void updateOrder(updateOrder updateOrder) {
	    if (updateOrder.getSerialId() == null) {
	        throw new IllegalArgumentException("SerialId cannot be null");
	    }
	    orderRepository.updateOrder(updateOrder);
	}

}
