package com.example.demo;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class GoogleLoginController {
	@Autowired
	private UserService userService;
	@PostMapping("/google-login")
	public ResponseEntity<?> loginWithGoogle(@RequestBody Map<String, String> body, HttpServletResponse response,
			HttpServletRequest request) {
		String token = body.get("token");

		GoogleIdToken.Payload payload = userService.verifyToken(token);
		
		if (payload == null || !Boolean.TRUE.equals(payload.getEmailVerified())) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

		String email = payload.getEmail();
        String name = (String) payload.get("name");
        String picture = (String) payload.get("picture");
        System.out.println(email);
        System.out.println(name);
        System.out.println(picture);
		String userAgent = request.getHeader("User-Agent");
		String ip = request.getHeader("X-Forwarded-For");
		if (ip == null)
			ip = request.getRemoteAddr();
		System.out.println(userAgent);
		System.out.println(ip);
		
		
        if(!userService.checkEmailExistOrNotInUsers(email)) {
        	if(picture!=null) {
        		userService.InsertUserDetailsFromGoogleLogin(email, name, picture);
        	}
        	else {
        		userService.InsertUserDetailsFromGoogleLoginWithoutImage(email, name);
        	}
        }
        else if(userService.checkEmailExistOrNotInUsers(email) && picture != null) {
        	userService.UpdateUserDetailsFromGoogleLogin(email, name, picture);
        }	

		Cookie cookie = new Cookie("token", userService.tokenBuilder(email, ip, userAgent));
		cookie.setHttpOnly(true);
		cookie.setSecure(false); 
		cookie.setPath("/");
		cookie.setMaxAge(7 * 24 * 60 * 60);
		response.addCookie(cookie);

		return ResponseEntity.ok(Map.of("message", "Login successful"));
	}

	
}
