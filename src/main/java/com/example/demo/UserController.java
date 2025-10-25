package com.example.demo;

import java.sql.Timestamp;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class UserController {

	@Autowired
	private UserService userService;
	@Autowired
	private NotificationService notificationService;

//	private final String CLIENT_ID = "421406248780-c6pukobh1dr5blgsa7u4ee7l3luml5iu.apps.googleusercontent.com";
	private final byte[] SECRET = Base64.getEncoder()
			.encode("sQe12Tg7Ld9BxkMfJpRzWuYx9AbVcDeFgHiJkLmNoPqRsTuVwXyZ1234567890ab".getBytes());

	@PostMapping("/send-code")
	public ResponseEntity<?> SendCode(@RequestParam String email) {
		return ResponseEntity.ok(userService.sendcode(email));
	}

	@PostMapping("/verify-email")
	public ResponseEntity<?> verifyCode(@RequestParam String email, @RequestParam String code,
			HttpServletResponse response, HttpServletRequest request) {
		Boolean flag = userService.verifyEmail(email, code);
		if (flag == true) {
			String userAgent = request.getHeader("User-Agent");
			String ip = request.getHeader("X-Forwarded-For");
			if (ip == null)
				ip = request.getRemoteAddr();
			System.out.println(userAgent);
			System.out.println(ip);

			String jwt = Jwts.builder().setSubject(email).claim("code", code).claim("ip", ip)
					.claim("userAgent", userAgent).signWith(Keys.hmacShaKeyFor(SECRET), SignatureAlgorithm.HS256)
					.compact();

			System.out.println(jwt);

			Cookie cookie = new Cookie("token_code", jwt);
			cookie.setHttpOnly(true);
			cookie.setSecure(false); // In production, set to true
			cookie.setPath("/");
			cookie.setMaxAge(10 * 60); // 10 minutes
			response.addCookie(cookie);
			if (userService.checkEmailExistOrNotInUsers(email) && userService.checkPasswordExistOrNotInUsers(email)) {
				return ResponseEntity.ok("/reset-password");
			}
			if (userService.checkEmailExistOrNotInUsers(email)) {
				return ResponseEntity.ok("/set-password");
			}
		}
		return ResponseEntity.ok("/user-details");
	}

	@PostMapping("/save-details")
	public ResponseEntity<?> SaveDetails(@RequestParam String name, @RequestParam String password,
			@RequestParam String confirmPassword, @RequestParam String imageData,
			@CookieValue(name = "token_code", required = false) String jwt, HttpServletRequest request,
			HttpServletResponse response) {
		String userAgent = request.getHeader("User-Agent");
		String ip = request.getHeader("X-Forwarded-For");
		if (ip == null)
			ip = request.getRemoteAddr();
		String email = userService.saveDetails(jwt, name, password, confirmPassword, imageData, ip, userAgent);
		Cookie cookie = new Cookie("token", userService.tokenBuilder(email, ip, userAgent));
		cookie.setHttpOnly(true);
		cookie.setSecure(false);
		cookie.setPath("/");
		cookie.setMaxAge(7 * 24 * 60 * 60);
		response.addCookie(cookie);
		return ResponseEntity.ok("Your Account created successfully.");
	}

	@PostMapping("/set-password")
	public ResponseEntity<?> setPassword(@RequestParam String password, @RequestParam String confirmPassword,
			@CookieValue(name = "token_code", required = false) String jwt, HttpServletRequest request,
			HttpServletResponse response) {
		String userAgent = request.getHeader("User-Agent");
		String ip = request.getHeader("X-Forwarded-For");
		if (ip == null)
			ip = request.getRemoteAddr();
		String email = userService.SetPassword(jwt, password, confirmPassword, ip, userAgent);
		Cookie cookie = new Cookie("token", userService.tokenBuilder(email, ip, userAgent));
		cookie.setHttpOnly(true);
		cookie.setSecure(false);
		cookie.setPath("/");
		cookie.setMaxAge(7 * 24 * 60 * 60);
		response.addCookie(cookie);
		return ResponseEntity.ok("Your password has been set successfully.");
	}

	@PostMapping("/send-code-to-reset-password")
	public ResponseEntity<?> SendCodeToResetPassword(@RequestParam String email) {
		return ResponseEntity.ok(userService.sendCodeToResetPassword(email));
	}

	@PostMapping("/reset-password")
	public ResponseEntity<?> resetPassword(@RequestParam String password, @RequestParam String confirmPassword,
			@CookieValue(name = "token_code", required = false) String jwt, HttpServletRequest request,
			HttpServletResponse response) {
		String userAgent = request.getHeader("User-Agent");
		String ip = request.getHeader("X-Forwarded-For");
		if (ip == null)
			ip = request.getRemoteAddr();
		String email = userService.ResetPassword(jwt, password, confirmPassword, ip, userAgent);
		Cookie cookie = new Cookie("token", userService.tokenBuilder(email, ip, userAgent));
		cookie.setHttpOnly(true);
		cookie.setSecure(false);
		cookie.setPath("/");
		cookie.setMaxAge(7 * 24 * 60 * 60);
		response.addCookie(cookie);
		return ResponseEntity.ok("Your password has been set successfully.");
	}

	@PostMapping("/login")
	public ResponseEntity<?> Login(@RequestParam String email, @RequestParam String password,
			HttpServletRequest request, HttpServletResponse response) {
		String userAgent = request.getHeader("User-Agent");
		String ip = request.getHeader("X-Forwarded-For");
		if (ip == null)
			ip = request.getRemoteAddr();
		if (userService.checkpassword(email, password)) {
			System.out.println(userService.tokenBuilder(email, ip, userAgent));
			Cookie cookie = new Cookie("token", userService.tokenBuilder(email, ip, userAgent));
//			cookie.setHttpOnly(true);
			cookie.setSecure(false);
			cookie.setPath("/");
			cookie.setMaxAge(7 * 24 * 60 * 60);
			response.addCookie(cookie);
		}
		return ResponseEntity.ok("Logged in successfully");
	}
	
//	@PostMapping("/admin-login")
//	public ResponseEntity<?> adminLogin(@RequestParam String email, @RequestParam String password,
//			HttpServletRequest request, HttpServletResponse response) {
//		String userAgent = request.getHeader("User-Agent");
//		String ip = request.getHeader("X-Forwarded-For");
//		if (ip == null)
//			ip = request.getRemoteAddr();
//		if (userService.checkpasswordAdmin(email, password) && userService.checkpassword(email, password)) {
//			System.out.println(userService.tokenBuilder(email, ip, userAgent));
//			Cookie cookie = new Cookie("admin_token", userService.tokenBuilderAdmin(email, ip, userAgent));
//			cookie.setHttpOnly(true);
//			cookie.setSecure(false);
//			cookie.setPath("/");
//			cookie.setMaxAge(7 * 24 * 60 * 60);
//			response.addCookie(cookie);
//		}
//		return ResponseEntity.ok("Logged in successfully");
//	}
	
	@PostMapping("/admin-login")
	public ResponseEntity<?> adminLogin(@RequestParam String email, @RequestParam String password,
			HttpServletRequest request, HttpServletResponse response) {
		String userAgent = request.getHeader("User-Agent");
		String ip = request.getHeader("X-Forwarded-For");
		if (ip == null)
			ip = request.getRemoteAddr();
		if (userService.checkpasswordAdmin(email, password) && userService.checkpassword(email, password)) {
			System.out.println(userService.tokenBuilder(email, ip, userAgent));
			Cookie cookie = new Cookie("admin_token", userService.tokenBuilderAdmin(email, ip, userAgent));
			cookie.setHttpOnly(true);
			cookie.setSecure(false);
			cookie.setPath("/");
			cookie.setMaxAge(7 * 24 * 60 * 60);
			response.addCookie(cookie);
			Cookie cookie1 = new Cookie("token", userService.tokenBuilder(email, ip, userAgent));
			cookie1.setHttpOnly(true);
			cookie1.setSecure(false);
			cookie1.setPath("/");
			cookie1.setMaxAge(7 * 24 * 60 * 60);
			response.addCookie(cookie1);
		}
		return ResponseEntity.ok("Logged in successfully");
	}

	@PostMapping("/logout")
	public ResponseEntity<?> logout(@CookieValue(name = "token", required = false) String jwt,
			HttpServletRequest request, HttpServletResponse response) {
		String userAgent = request.getHeader("User-Agent");
		String ip = request.getHeader("X-Forwarded-For");
		if (ip == null)
			ip = request.getRemoteAddr();
		if(userService.checkTokenValidity(jwt, ip, userAgent)) {
			Cookie cookie = new Cookie("token", null);
			cookie.setHttpOnly(true);
			cookie.setSecure(false);
			cookie.setPath("/");
			cookie.setMaxAge(0);
			response.addCookie(cookie);
			return ResponseEntity.ok("Logged out successfully");
		}
		
		return ResponseEntity.ok("Logged out failed");
	}
	
	@PostMapping("/admin-logout")
	public ResponseEntity<?> adminLogout(@CookieValue(name = "token", required = false) String jwt,
			@CookieValue(name = "admin_token", required = false) String jwt1,
			HttpServletRequest request, HttpServletResponse response) {
		String userAgent = request.getHeader("User-Agent");
		String ip = request.getHeader("X-Forwarded-For");
		if (ip == null)
			ip = request.getRemoteAddr();
		if(userService.checkTokenValidity(jwt, ip, userAgent) && userService.checkTokenValidityAdmin(jwt1, ip, userAgent)) {
			Cookie cookie = new Cookie("token", null);
			cookie.setHttpOnly(true);
			cookie.setSecure(false);
			cookie.setPath("/");
			cookie.setMaxAge(0);
			response.addCookie(cookie);
			Cookie cookie1 = new Cookie("admin_token", null);
			cookie1.setHttpOnly(true);
			cookie1.setSecure(false);
			cookie1.setPath("/");
			cookie1.setMaxAge(0);
			response.addCookie(cookie1);
			return ResponseEntity.ok("Logged out successfully");
		}
		
		return ResponseEntity.badRequest().body("Logged out failed!");
	}
	
	@PostMapping("/checkToken")
	public ResponseEntity<?> checkToken(@CookieValue(name = "token", required = false) String jwt,
	        HttpServletRequest request, HttpServletResponse response) {
	    String userAgent = request.getHeader("User-Agent");
	    String ip = request.getHeader("X-Forwarded-For");
	    if (ip == null)
	        ip = request.getRemoteAddr();
	    
	    boolean isTokenValid = userService.checkTokenValidity(jwt, ip, userAgent);
	    return ResponseEntity.ok(Map.of("isTokenValid", isTokenValid));  // Return the validity status
	}
	
	@GetMapping("/user-details")
	public ResponseEntity<?> userDetails(@CookieValue(name = "token", required = false) String jwt,
	        HttpServletRequest request, HttpServletResponse response) {
	    String userAgent = request.getHeader("User-Agent");
	    String ip = request.getHeader("X-Forwarded-For");
	    if (ip == null)
	        ip = request.getRemoteAddr();
   
	    if(userService.checkTokenValidity(jwt, ip, userAgent)) {
	    	return ResponseEntity.ok(userService.GetUserDetailsByEmail(userService.getEmailFromToken(jwt, ip, userAgent)));
	    }
	    
	    return ResponseEntity.ok("Unreachable");
	}
	
	@GetMapping("/admin-validity")
	public ResponseEntity<?> adminDetails(@CookieValue(name = "token", required = false) String jwt,
			@CookieValue(name = "admin_token", required = false) String jwt1,
	        HttpServletRequest request, HttpServletResponse response) {
	    String userAgent = request.getHeader("User-Agent");
	    String ip = request.getHeader("X-Forwarded-For");
	    if (ip == null)
	        ip = request.getRemoteAddr();
   
	    if(userService.checkTokenValidity(jwt, ip, userAgent) && userService.checkTokenValidityAdmin(jwt1, ip, userAgent)) {
	    	return ResponseEntity.ok("Token is valid.");
	    }	   
	    else {
	    	return ResponseEntity.badRequest().body("Admin is not valid!");
	    }
	   
	}
	
	@PostMapping("/edit-profile")
	public ResponseEntity<?> UpdateUserDetailss(@RequestParam String name, @RequestParam String imageData,
			@CookieValue(name = "token", required = false) String jwt, HttpServletRequest request) {
		String userAgent = request.getHeader("User-Agent");
		String ip = request.getHeader("X-Forwarded-For");
		if (ip == null)
			ip = request.getRemoteAddr();
		if(userService.checkTokenValidity(jwt, ip, userAgent)) {
			System.out.println(userService.getEmailFromToken(jwt, ip, userAgent));
			userService.UpdateUserDetails(userService.getEmailFromToken(jwt, ip, userAgent), name, imageData);
		}
		
		return ResponseEntity.ok("Your profile updated successfully.");
	}

	@PostMapping("/change-password")
	public ResponseEntity<?> changePassword(@RequestParam String currentPassword,@RequestParam String newPassword, @RequestParam String confirmPassword,
			@CookieValue(name = "token", required = false) String jwt, HttpServletRequest request,
			HttpServletResponse response) {
		String userAgent = request.getHeader("User-Agent");
		String ip = request.getHeader("X-Forwarded-For");
		if (ip == null)
			ip = request.getRemoteAddr();
		userService.ChangePassword(jwt, currentPassword, newPassword,confirmPassword, ip, userAgent);
		return ResponseEntity.ok("Your password has been update successfully.");
	}
	
	@PostMapping("/change-admin-password")
	public ResponseEntity<?> changeAdminPassword(@RequestParam String currentPassword,@RequestParam String newPassword, @RequestParam String confirmPassword,
			@CookieValue(name = "token", required = false) String jwt, HttpServletRequest request,
			@CookieValue(name = "admin_token", required = false) String jwt1,
			HttpServletResponse response) {
		 String userAgent = request.getHeader("User-Agent");
		    String ip = request.getHeader("X-Forwarded-For");
		    if (ip == null)
		        ip = request.getRemoteAddr();
	   
		    if(userService.checkTokenValidityAdmin(jwt1, ip, userAgent)) {
		    	userService.ChangeAdminPassword(jwt, currentPassword, newPassword,confirmPassword, ip, userAgent);
		    	notificationService.createActivityLog(ActivityLog.builder()
				        .id(UUID.randomUUID().toString())
				        .title(String.format("Password Updated"))
				        .message(String.format("Your password Updated successfully."))
				        .timestamp(new Timestamp(System.currentTimeMillis()))
				        .type("password-update")
				        .build());
				return ResponseEntity.ok("Your password has been update successfully.");
		    }	   
		    else {
		    	return ResponseEntity.badRequest().body("Admin is not valid!");
		    }
	}
	
	@GetMapping("/api/user/SocketDestination")
    public ResponseEntity<String> getEmail(
            @CookieValue(name = "token", required = false) String jwt,
            HttpServletRequest request) {
        try {
            String userAgent = request.getHeader("User-Agent");
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null) {
                ip = request.getRemoteAddr();
            }
            if (userService.checkTokenValidity(jwt, ip, userAgent)) {
                return ResponseEntity.ok(userService.getWebSocketUUIDByEmail(userService.getEmailFromToken(jwt, ip, userAgent)));
            } else {
                throw new IllegalArgumentException("Your session has expired or the token is invalid. Please log in again to continue.");
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
	
	
}
