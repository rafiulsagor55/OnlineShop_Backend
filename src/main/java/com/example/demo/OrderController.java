package com.example.demo;

import java.sql.Timestamp;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
	@Autowired
	NotificationService notificationService;
	
	@Autowired
	private SimpMessagingTemplate messagingTemplate;

	@PostMapping("/save-order")
	public ResponseEntity<?> SaveOrder(@RequestBody Order order,
	                                   @CookieValue(name = "token", required = false) String jwt, 
	                                   HttpServletRequest request){
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
		
//		notificationService.createNotification(Notification.builder()
//                .id(UUID.randomUUID().toString())
//                .email(order.getEmail())
//                .title("Order Placed")
//                .message(String.format("Your order ORD-%s has been successfully placed.", orderService.getSerialIdbyUUID(order.getUUID())))
//                .timestamp(new Timestamp(System.currentTimeMillis()))
//                .read(false)
//                .type("order")
//                .build());
		
		return ResponseEntity.ok("Order placed successfully.");
	}
	
	@PostMapping("/update-order")
    public ResponseEntity<?> updateOrder(@RequestBody updateOrder updateOrder,
                                        @CookieValue(name = "admin_token", required = false) String jwt,
                                        HttpServletRequest request) {
        try {
            String userAgent = request.getHeader("User-Agent");
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null) {
                ip = request.getRemoteAddr();
            }

            if (userService.checkTokenValidityAdmin(jwt, ip, userAgent)) {
                // Update the order in the database
                orderService.updateOrder(updateOrder);

                // Get the status from updateOrder
                String status = updateOrder.getStatus();
                String notificationTitle = "";
                String notificationMessage = "";
                String notificationMessageAdmin = "";
                String type="";

                switch (status) {
                    case "Order Placed":
                        notificationTitle = "Order Placed";
                        notificationMessage = String.format("Your order ORD-%s has been successfully placed.", updateOrder.getSerialId());
                        notificationMessageAdmin = String.format("Order ORD-%s has been successfully placed.", updateOrder.getSerialId());
                        type="Order Placed";
                        break;
                    case "processing":
                        notificationTitle = "Order Processing";
                        notificationMessage = String.format("Your order ORD-%s is now being processed.", updateOrder.getSerialId());
                        notificationMessageAdmin = String.format("Order ORD-%s is now being processed.", updateOrder.getSerialId());
                        type="processing";
                        break;
                    case "ready":
                        notificationTitle = "Order Ready for Pickup";
                        notificationMessage = String.format("Your order ORD-%s is ready for pickup at our store (Mirpur-10, Dhaka).", updateOrder.getSerialId());
                        notificationMessageAdmin = String.format("Order ORD-%s is ready for pickup at our store (Mirpur-10, Dhaka).", updateOrder.getSerialId());
                        type="ready";
                        break;
                    case "shipped":
                        notificationTitle = "Order Shipped";
                        notificationMessage = String.format("Your order ORD-%s has been shipped. Tracking number: DHL-%s.", updateOrder.getSerialId(), generateTrackingNumber());
                        notificationMessageAdmin = String.format("Order ORD-%s has been shipped. Tracking number: DHL-%s.", updateOrder.getSerialId(), generateTrackingNumber());
                        type="shipped";
                        break;
                    case "picked":
                        notificationTitle = "Order Picked Up";
                        notificationMessage = String.format("Your order ORD-%s has been successfully picked up.", updateOrder.getSerialId());
                        notificationMessageAdmin = String.format("Order ORD-%s has been successfully picked up.", updateOrder.getSerialId());
                        type="picked";
                        break;
                    case "delivered":
                        notificationTitle = "Order Delivered";
                        notificationMessage = String.format("Your order ORD-%s has been delivered. Thank you for shopping with us!", updateOrder.getSerialId());
                        notificationMessageAdmin = String.format("Order ORD-%s has been delivered. Thank you for shopping with us!", updateOrder.getSerialId());
                        type="delivered";
                        break;
                    case "cancelled":
                        notificationTitle = "Order Cancelled";
                        notificationMessage = String.format("Your order ORD-%s has been cancelled.", updateOrder.getSerialId());
                        notificationMessageAdmin = String.format("Order ORD-%s has been cancelled.", updateOrder.getSerialId());
                        type="cancelled";
                        break;
                    default:
                        return ResponseEntity.ok("Order updated successfully.");
                }

                // Send notification to the customer
                messagingTemplate.convertAndSendToUser(
                        userService.getWebSocketUUIDByEmail(orderService.getEmailBySerialId(updateOrder.getSerialId())),
                        "/notification",
                        Notification.builder()
                                .id(UUID.randomUUID().toString())
                                .title(notificationTitle)
                                .message(notificationMessage)
                                .timestamp(new Timestamp(System.currentTimeMillis()))
                                .read(false)
                                .type(type)
                                .serialId(updateOrder.getSerialId())
                                .build()
                );
                notificationService.createNotification(Notification.builder()
                                .id(UUID.randomUUID().toString())
                                .email(orderService.getEmailBySerialId(updateOrder.getSerialId()))
                                .title(notificationTitle)
                                .message(notificationMessage)
                                .timestamp(new Timestamp(System.currentTimeMillis()))
                                .read(false)
                                .type(type)
                                .serialId(updateOrder.getSerialId())
                                .build());
                notificationService.createActivityLog(ActivityLog.builder()
                		.id(UUID.randomUUID().toString())
                		.title(notificationTitle)
                        .message(notificationMessageAdmin)
                        .timestamp(new Timestamp(System.currentTimeMillis()))
                        .type(type)
                		.build());
                

                return ResponseEntity.ok(notificationMessage);
            } else {
                throw new IllegalArgumentException("Your session has expired or the token is invalid. Please log in again to continue.");
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("An error occurred while updating the order: " + e.getMessage());
        }
    }

    // Helper method to generate a tracking number
    private String generateTrackingNumber() {
        return String.valueOf(100000000 + (int)(Math.random() * 900000000));
    }
	
	@GetMapping("/get-all-order-item")
	public ResponseEntity<?> GetorderItems(@CookieValue(name = "token", required = false) String jwt, HttpServletRequest request){
		try {
			String userAgent = request.getHeader("User-Agent");
			String ip = request.getHeader("X-Forwarded-For");
			if (ip == null)
				ip = request.getRemoteAddr();
			if(userService.checkTokenValidity(jwt, ip, userAgent)) {
				return ResponseEntity.ok(orderService.getOrdersWithItemsAndProductDetailsByEmail(
				        userService.getEmailFromToken(jwt, ip, userAgent)));
			}
			else {
				throw new IllegalArgumentException("Your session has expired or the token is invalid. Please log in again to continue.");
			}	
		} catch (Exception e) {
			throw new IllegalArgumentException(e.getMessage());
		}			
	}
	
	@GetMapping("/get-order-item/{id}")
	public ResponseEntity<?> GetorderItemById(@PathVariable Long id,@CookieValue(name = "token", required = false) String jwt, HttpServletRequest request){
		try {
			String userAgent = request.getHeader("User-Agent");
			String ip = request.getHeader("X-Forwarded-For");
			if (ip == null)
				ip = request.getRemoteAddr();
			if(userService.checkTokenValidity(jwt, ip, userAgent)) {
				return ResponseEntity.ok(orderService.getOrdersWithItemsAndProductDetailsByEmailAndId(
				        userService.getEmailFromToken(jwt, ip, userAgent),id));
			}
			else {
				throw new IllegalArgumentException("Your session has expired or the token is invalid. Please log in again to continue.");
			}	
		} catch (Exception e) {
			throw new IllegalArgumentException(e.getMessage());
		}			
	}

	
	@GetMapping("/get-all-order-item-admin")
	public ResponseEntity<?> GetorderItemsAdmin(@CookieValue(name = "admin_token", required = false) String jwt, HttpServletRequest request){
		try {
			String userAgent = request.getHeader("User-Agent");
			String ip = request.getHeader("X-Forwarded-For");
			if (ip == null)
				ip = request.getRemoteAddr();
			if(userService.checkTokenValidityAdmin(jwt, ip, userAgent)) {
				return ResponseEntity.ok(orderService.getOrdersWithItemsAndProductDetails());
			}
			else {
				throw new IllegalArgumentException("Your session has expired or the token is invalid. Please log in again to continue.");
			}	
		} catch (Exception e) {
			throw new IllegalArgumentException(e.getMessage());
		}			
	}
}
