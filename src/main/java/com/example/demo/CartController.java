package com.example.demo;

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
public class CartController {
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private CartService cartService;

	@PostMapping("/add-to-cart")
	public ResponseEntity<?>AddToCartt(@RequestBody Cart cart,@CookieValue(name = "token", required = false) String jwt, HttpServletRequest request){
		String userAgent = request.getHeader("User-Agent");
		String ip = request.getHeader("X-Forwarded-For");
		if (ip == null)
			ip = request.getRemoteAddr();
		if(userService.checkTokenValidity(jwt, ip, userAgent)) {
			cart.setEmail(userService.getEmailFromToken(jwt, ip, userAgent));
			if(cartService.IsProductExistForSpecificId(cart.getProductId())) {
				cartService.addToCart(cart);
				return ResponseEntity.ok("Product added to cart successfully!");
			}
			else {
				throw new IllegalArgumentException("No product exists for this Product ID.");
			}
		}
		else {
			throw new IllegalArgumentException("Your session has expired or the token is invalid. Please log in again to continue.");
		}
				
	}
	
	@GetMapping("/get-all-cart-item")
	public ResponseEntity<?>GetCartItems(@CookieValue(name = "token", required = false) String jwt, HttpServletRequest request){
		try {
			String userAgent = request.getHeader("User-Agent");
			String ip = request.getHeader("X-Forwarded-For");
			System.out.println(ip);
			if (ip == null)
				ip = request.getRemoteAddr();
			if(userService.checkTokenValidity(jwt, ip, userAgent)) {
				return ResponseEntity.ok(cartService.GetCartItemsByEmail(userService.getEmailFromToken(jwt, ip, userAgent)));
			}
			else {
				throw new IllegalArgumentException("Your session has expired or the token is invalid. Please log in again to continue.");
			}	
		} catch (Exception e) {
			throw new IllegalArgumentException(e.getMessage());
		}			
	}
}
