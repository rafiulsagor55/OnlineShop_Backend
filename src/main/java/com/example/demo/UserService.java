package com.example.demo;

import java.util.Base64;
import java.util.Collections;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Service
public class UserService {

	@Autowired
	private JavaMailSender mailSender;

	@Autowired
	private UserRepository userRepository;
	private final String CLIENT_ID = "421406248780-c6pukobh1dr5blgsa7u4ee7l3luml5iu.apps.googleusercontent.com";
	private final byte[] SECRET = Base64.getEncoder()
			.encode("sQe12Tg7Ld9BxkMfJpRzWuYx9AbVcDeFgHiJkLmNoPqRsTuVwXyZ1234567890ab".getBytes());

	public String generateVerificationCode() {
		return String.format("%06d", new Random().nextInt(999999)); // Generate a 6-digit code
	}

	public void sendVerificationCode(String email) {
		String code = generateVerificationCode();
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(email);
		message.setSubject("Your Verification Code for Online Shop.");
		message.setText("Your code is: " + code);
		mailSender.send(message);
		userRepository.insertCode(email, code);
	}

	public String sendcode(String email) {
		if (!userRepository.doesEmailExistWithPassword(email)) {
			userRepository.deleteByEmail(email);
			sendVerificationCode(email);
			return "Verification code sent to: " + email;
		} else {
			throw new IllegalArgumentException("You already have an account! Please Sign in.");
		}
	}

	public Boolean verifyEmail(String email, String code) {
		int count = userRepository.getCount(email);
		if (count == -1) {
			throw new IllegalArgumentException("Email does not exist!");
		} else if (count >= 5) {
			throw new IllegalArgumentException("Too many incorrect attempts! please resend your code and try again.");
		} else if (!userRepository.doesCodeExistForEmail(email, code)) {
			userRepository.incrementCount(email);
			throw new IllegalArgumentException("Invalid verification code! Please try again.");
		} else {
//    		userRepository.deleteByEmail(email);
			return true;
		}

	}

	public String saveDetails(String jwt, String name, String password, String confirmPassword, String imageData,
			String ip, String userAgent) {
		if (jwt == null)
			throw new IllegalArgumentException("Something went wrong! please resend your code and try again.");

		try {
			Claims claims = Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(SECRET)).build().parseClaimsJws(jwt)
					.getBody();
			Boolean flag = userRepository.doesCodeExistForEmail(claims.getSubject(), (String) claims.get("code"));

			String ipClaim = (String) claims.get("ip");
			String userAgentClaim = (String) claims.get("userAgent");

			if (ip != null && ip.equals(ipClaim) && userAgent != null && userAgent.equals(userAgentClaim)) {
				if (!flag) {
					throw new IllegalArgumentException("Something went wrong! please resend your code and try again.");
				}
				if (password.length() < 4) {
					throw new IllegalArgumentException("Password must be at least 4 characters!");
				}
				if (!password.equals(confirmPassword)) {
					throw new IllegalArgumentException("Password and confirm password do not match!");
				}
				if (name.length() < 2 || name.length() > 30) {
					throw new IllegalArgumentException("Name must be between 2 and 30 characters!");
				}
				if (!userRepository.doesEmailExist(claims.getSubject())) {
					byte[] decodedBytes = null;
			        String contentType = null;
			        
			        if (imageData != null && imageData.startsWith("data:")) {
			            int commaIndex = imageData.indexOf(",");
			            if (commaIndex != -1) {
			                contentType = imageData.substring(5, commaIndex); // Extract MIME type
			                imageData = imageData.substring(commaIndex + 1); // Remove the base64 prefix
			                decodedBytes = Base64.getDecoder().decode(imageData); // Decode base64
			            }
			        }
			        
			        // Validate that we have valid data
			        if (decodedBytes == null || decodedBytes.length == 0) {
			            throw new IllegalArgumentException("Invalid image data.");
			        }

					userRepository.insertUserDetails(claims.getSubject(), name, decodedBytes,contentType);
					userRepository.insertPassword(claims.getSubject(), password);
					userRepository.deleteByEmail(claims.getSubject());
					return claims.getSubject();
				} else {
					throw new IllegalArgumentException("Something went wrong!");
				}

			} else {
				throw new IllegalArgumentException("Something went wrong! please resend your code and try again.");
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalArgumentException(e.getMessage());
		}

	}

	public Boolean checkEmailExistOrNotInUsers(String email) {
		return userRepository.doesEmailExist(email);
	}

	public Boolean checkPasswordExistOrNotInUsers(String email) {
		return userRepository.doesEmailExistWithPassword(email);
	}

	public String SetPassword(String jwt, String password, String confirmPassword, String ip, String userAgent) {
		if (jwt == null)
			throw new IllegalArgumentException("Something went wrong! please resend your code and try again.");
		try {
			Claims claims = Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(SECRET)).build().parseClaimsJws(jwt)
					.getBody();
			Boolean flag = userRepository.doesCodeExistForEmail(claims.getSubject(), (String) claims.get("code"));

			String ipClaim = (String) claims.get("ip");
			String userAgentClaim = (String) claims.get("userAgent");

			if (ip != null && ip.equals(ipClaim) && userAgent != null && userAgent.equals(userAgentClaim)) {
				if (!flag) {
					throw new IllegalArgumentException("Something went wrong! please resend your code and try again.");
				}
				if (password.length() < 4) {
					throw new IllegalArgumentException("Password must be at least 4 characters!");
				}
				if (!password.equals(confirmPassword)) {
					throw new IllegalArgumentException("Password and confirm password do not match!");
				}
				if (!userRepository.doesEmailExistWithPassword(claims.getSubject())) {
					userRepository.insertPassword(claims.getSubject(), password);
					userRepository.deleteByEmail(claims.getSubject());
					return claims.getSubject();
				} else {
					throw new IllegalArgumentException("Something went wrong!");
				}
			} else {
				throw new IllegalArgumentException("Something went wrong! please resend your code and try again.");
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalArgumentException(e.getMessage());
		}
	}

	public String sendCodeToResetPassword(String email) {
		if (userRepository.doesEmailExistWithPassword(email)) {
			userRepository.deleteByEmail(email);
			sendVerificationCode(email);
			return "Verification code sent to: " + email;
		} else {
			throw new IllegalArgumentException("Account does not exist for this Email!");
		}
	}

	public String ResetPassword(String jwt, String password, String confirmPassword, String ip, String userAgent) {
		if (jwt == null)
			throw new IllegalArgumentException("Something went wrong! please resend your code and try again.");
		try {
			Claims claims = Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(SECRET)).build().parseClaimsJws(jwt)
					.getBody();
			Boolean flag = userRepository.doesCodeExistForEmail(claims.getSubject(), (String) claims.get("code"));

			String ipClaim = (String) claims.get("ip");
			String userAgentClaim = (String) claims.get("userAgent");

			if (ip != null && ip.equals(ipClaim) && userAgent != null && userAgent.equals(userAgentClaim)) {
				if (!flag) {
					throw new IllegalArgumentException("Something went wrong! please resend your code and try again.");
				}
				if (password.length() < 4) {
					throw new IllegalArgumentException("Password must be at least 4 characters!");
				}
				if (!password.equals(confirmPassword)) {
					throw new IllegalArgumentException("Password and confirm password do not match!");
				}
				if (userRepository.doesEmailExistWithPassword(claims.getSubject())) {
					userRepository.updatePassword(claims.getSubject(), password);
					userRepository.deleteByEmail(claims.getSubject());
					return claims.getSubject();
				} else {
					throw new IllegalArgumentException("Something went wrong!");
				}
			} else {
				throw new IllegalArgumentException("Something went wrong! please resend your code and try again.");
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new IllegalArgumentException(e.getMessage());
		}
	}

	public String tokenBuilder(String email, String ip, String userAgent) {
		return Jwts.builder().setSubject(email).claim("ip", ip).claim("userAgent", userAgent)
				.signWith(Keys.hmacShaKeyFor(SECRET), SignatureAlgorithm.HS256).compact();
	}

	public Boolean checkpassword(String email, String password) {
		Boolean flag=userRepository.checkPassword(email, password);
		if(flag)return true;
		else throw new IllegalArgumentException("Invalid email or password");
	}

	public String getEmailFromToken(String jwt, String ip, String userAgent) {
		if (jwt == null)
			return null;
		try {
			Claims claims = Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(SECRET)).build().parseClaimsJws(jwt)
					.getBody();

			String ipClaim = (String) claims.get("ip");
			String userAgentClaim = (String) claims.get("userAgent");
			if (ip != null && ip.equals(ipClaim) && userAgent != null && userAgent.equals(userAgentClaim)
					&& userRepository.doesEmailExist(claims.getSubject())) {
				return claims.getSubject();
			}
			return null;
		} catch (Exception e) {
			return null;
		}
	}

	public Boolean checkTokenValidity(String jwt, String ip, String userAgent) {
		if (jwt == null)
			return false;
		try {
			Claims claims = Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(SECRET)).build().parseClaimsJws(jwt)
					.getBody();

			String ipClaim = (String) claims.get("ip");
			String userAgentClaim = (String) claims.get("userAgent");
			if (ip != null && ip.equals(ipClaim) && userAgent != null && userAgent.equals(userAgentClaim)
					&& userRepository.doesEmailExist(claims.getSubject())) {
				return true;
			}
			return false;
		} catch (Exception e) {
			return false;
		}
	}
	
	public GoogleIdToken.Payload verifyToken(String token) {
		try {
			GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(),
					GsonFactory.getDefaultInstance()).setAudience(Collections.singletonList(CLIENT_ID)).build();

			GoogleIdToken idToken = verifier.verify(token);
			return (idToken != null) ? idToken.getPayload() : null;
		} catch (Exception e) {
			return null;
		}
	}
	
	public void InsertUserDetailsFromGoogleLogin(String email, String name, String image_path) {
		try {
			userRepository.insertUserDetailsFromGoogleLogin(email, name, image_path);
		}catch (Exception e) {
			throw new IllegalArgumentException("Something went wrong! please try again later.");
		}
	}
	
	public void InsertUserDetailsFromGoogleLoginWithoutImage(String email, String name) {
		try {
			userRepository.insertUserDetailsFromGoogleLoginWithoutImage(email, name);
		}catch (Exception e) {
			throw new IllegalArgumentException("Something went wrong! please try again later.");
		}
	}
	
	public void UpdateUserDetailsFromGoogleLogin(String email, String name, String image_path) {
		try {
			userRepository.updateUserDetailsFromGoogleLogin(email, name, image_path);
		}catch (Exception e) {
			throw new IllegalArgumentException("Something went wrong! please try again later.");
		}
	}
	
	public UsersDTO GetUserDetailsByEmail(String email) {
		try {
			Users user=userRepository.getUserDetailsByEmail(email);
			String reconstructedDataUrl=null;
			if(user.getContentType()!=null) {
				String reEncodedBase64 = Base64.getEncoder().encodeToString(user.getImageData());
				reconstructedDataUrl = "data:" + user.getContentType() + ";base64," + reEncodedBase64;
			}
			return UsersDTO.builder().email(user.getEmail()).name(user.getName()).imageData(reconstructedDataUrl)
					.imagePath(user.getImagePath()).contentType(user.getContentType()).build();
		} catch (Exception e) {
			throw new RuntimeException("Error fetching data from database!");
		}
	}
	
	public void UpdateUserDetails(String email, String name, String imageData) {
	    try {
	        byte[] decodedBytes = null;
	        String contentType = null;
	        
	        if (imageData != null && imageData.startsWith("data:")) {
	            int commaIndex = imageData.indexOf(",");
	            if (commaIndex != -1) {
	                contentType = imageData.substring(5, commaIndex); // Extract MIME type
	                imageData = imageData.substring(commaIndex + 1); // Remove the base64 prefix
	                decodedBytes = Base64.getDecoder().decode(imageData); // Decode base64
	            }
	        }
	        
	        // Validate that we have valid data
	        if (decodedBytes == null || decodedBytes.length == 0) {
	            throw new IllegalArgumentException("Invalid image data.");
	        }

	        // Update user details in the database
	        userRepository.updateUserDetails(email, name, decodedBytes, contentType);
	    } catch (Exception e) {
	        e.printStackTrace(); // Print stack trace to debug
	        throw new RuntimeException("Error occurred to update data!");
	    }
	}
	
	public String ChangePassword(String jwt,String currentPassword, String password, String confirmPassword, String ip, String userAgent) {
		if (jwt == null)
			throw new IllegalArgumentException("Something went wrong! please try again later.");
		try {
			Claims claims = Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(SECRET)).build().parseClaimsJws(jwt)
					.getBody();
			String ipClaim = (String) claims.get("ip");
			String userAgentClaim = (String) claims.get("userAgent");

			if (ip != null && ip.equals(ipClaim) && userAgent != null && userAgent.equals(userAgentClaim)) {
				if(!userRepository.checkPassword(claims.getSubject(), currentPassword)) {
					throw new IllegalArgumentException("Wrong current password!");
				}
				if (password.length() < 4) {
					throw new IllegalArgumentException("Password must be at least 4 characters!");
				}
				if (!password.equals(confirmPassword)) {
					throw new IllegalArgumentException("Password and confirm password do not match!");
				}
				if (password.equals(currentPassword)) {
					throw new IllegalArgumentException("New password must be different from the current password.");
				}
				if (userRepository.doesEmailExistWithPassword(claims.getSubject())) {
					userRepository.updatePassword(claims.getSubject(), password);
					return claims.getSubject();
				} else {
					throw new IllegalArgumentException("Something went wrong!");
				}
			} else {
				throw new IllegalArgumentException("Something went wrong! please try again later.");
			}

		} catch (Exception e) {
//			e.printStackTrace();
			throw new IllegalArgumentException(e.getMessage());
		}
	}


}
