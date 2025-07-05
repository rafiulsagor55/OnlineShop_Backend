package com.example.demo;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class OrderController {
	
	@Autowired
	UserService userService;
	@Autowired
	OrderService orderService;

	@PostMapping("/save-order")
	public ResponseEntity<?>SaveOrder(@RequestBody Order order,@CookieValue(name = "token", required = false) String jwt, HttpServletRequest request){
		String userAgent = request.getHeader("User-Agent");
		String ip = request.getHeader("X-Forwarded-For");
		if (ip == null)
			ip = request.getRemoteAddr();
		if(userService.checkTokenValidity(jwt, ip, userAgent)) {
			order.setEmail(userService.getEmailFromToken(jwt, ip, userAgent));
			if(order.getPaymentMethod().equals("COD")) {
				order.setPayment("Pending");
			}
			else {
				order.setPayment("Paid");
			}
			order.setUUID(UUID.randomUUID().toString());
			order.setStatus("Order Placed");
			orderService.saveOrder(order);
		}
		return ResponseEntity.ok("Order placed successfully.");
	}
	
	@GetMapping("/get-all-order-item")
	public ResponseEntity<?>GetorderItems(@CookieValue(name = "token", required = false) String jwt, HttpServletRequest request){
		try {
			String userAgent = request.getHeader("User-Agent");
			String ip = request.getHeader("X-Forwarded-For");
			System.out.println(ip);
			if (ip == null)
				ip = request.getRemoteAddr();
			if(userService.checkTokenValidity(jwt, ip, userAgent)) {
				return ResponseEntity.ok(orderService.getOrdersWithItemsAndProductDetailsByEmail((userService.getEmailFromToken(jwt, ip, userAgent))));
			}
			else {
				throw new IllegalArgumentException("Your session has expired or the token is invalid. Please log in again to continue.");
			}	
		} catch (Exception e) {
			throw new IllegalArgumentException(e.getMessage());
		}			
	}
}
