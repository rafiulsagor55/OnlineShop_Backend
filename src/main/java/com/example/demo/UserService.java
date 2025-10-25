package com.example.demo;

import java.util.Base64;
import java.util.Collections;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.mail.internet.MimeMessage;
import java.util.regex.Pattern;


@Service
public class UserService {

	@Autowired
	private JavaMailSender mailSender;

	@Autowired
	private UserRepository userRepository;
	private final String CLIENT_ID = "421406248780-c6pukobh1dr5blgsa7u4ee7l3luml5iu.apps.googleusercontent.com";
	private final byte[] SECRET = Base64.getEncoder()
			.encode("sQe12Tg7Ld9BxkMfJpRzWuYx9AbVcDeFgHiJkLmNoPqRsTuVwXyZ1234567890ab".getBytes());
	
	private final byte[] SECRET_ADMIN = Base64.getEncoder()
			.encode("aZx9Yw8Vx7Ut6Sr5Qp4No3Lm2Jk1Hg0Fi9Ed8Cb7Wa6Tv5Ru4Mt3Lp2Kn1".getBytes());

	public String generateVerificationCode() {
		return String.format("%06d", new Random().nextInt(999999)); // Generate a 6-digit code
	}

	
	
	public void sendVerificationCode(String email) {
	    String code = generateVerificationCode();

	    try {
	        MimeMessage message = mailSender.createMimeMessage();
	        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
	        helper.setFrom("rafeulsagor@example.com", "Online Shop");
	        helper.setTo(email);
	        helper.setSubject("Your Verification Code for Online Shop");	        

	        String htmlContent = """
	                <!DOCTYPE html>
	                <html lang="en">
	                <head>
	                    <meta charset="UTF-8">
	                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
	                    <style>
	                        body { margin: 0; padding: 0; background-color: #e8ecef; font-family: 'Inter', 'Helvetica Neue', Arial, sans-serif; }
	                        .container { max-width: 600px; margin: 20px auto; background: #ffffff; border-radius: 12px; box-shadow: 0 8px 24px rgba(0,0,0,0.12); overflow: hidden; }
	                        .header { background: linear-gradient(135deg, #0d4238, #586c90); padding: 20px; text-align: center; color: #ffffff; }
	                        .header h1 { margin: 0; font-size: 28px; font-weight: 600; letter-spacing: 0.5px; }
	                        .content { padding: 20px; color: #1f2a44; }
	                        .content p { font-size: 16px; line-height: 1.7; margin: 0 0 16px; }
	                        .code-box { background: #eef2f7; border: 2px solid #055858; border-radius: 8px; padding: 16px; text-align: center; margin: 16px 0; box-shadow: 0 4px 12px rgba(0,0,0,0.08); }
	                        .code { font-size: 36px; font-weight: 700; color: #055858; letter-spacing: 5px; text-transform: uppercase; }
	                        .note { font-size: 14px; color: #586c90; margin-top: 8px; font-style: italic; }
	                        .footer { background: #eef2f7; padding: 16px; text-align: center; font-size: 14px; color: #586c90; border-top: 1px solid #055858; }
	                        .footer a { color: #055858; text-decoration: none; font-weight: 500; }
	                        .footer a:hover { text-decoration: underline; }
	                        @media (max-width: 600px) { 
	                            .container { margin: 10px; border-radius: 10px; }
	                            .header { padding: 16px; }
	                            .header h1 { font-size: 22px; }
	                            .content { padding: 16px; }
	                            .code { font-size: 30px; letter-spacing: 3px; }
	                        }
	                    </style>
	                </head>
	                <body>
	                    <div class="container">
	                        <div class="header">
	                            <h1>Online Shop Verification</h1>
	                        </div>
	                        <div class="content">
	                            <p>Dear Customer,</p>
	                            <p>Thank you for choosing Online Shop. To complete your verification process, please use the following one-time verification code:</p>
	                            <div class="code-box">
	                                <div class="code">%s</div>
	                                <div class="note">This code is valid for 10 minutes</div>
	                                <div class="note">Don't share this code with anyone</div>
	                            </div>
	                            <p>If you did not initiate this request, please disregard this email or reach out to our support team for assistance.</p>
	                        </div>
	                        <div class="footer">
	                            <p>Best Regards,<br>The Online Shop Team</p>
	                            <p><a href="https://www.onlineshop.com/support">Contact Support</a> | <a href="https://www.onlineshop.com">Visit Our Website</a></p>
	                        </div>
	                    </div>
	                </body>
	                </html>
	                """.formatted(code);

	        helper.setText(htmlContent, true);

	        mailSender.send(message);
	        userRepository.insertCode(email, code);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}

	public String sendcode(String email){
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
	
	public String tokenBuilderAdmin(String email, String ip, String userAgent) {
		return Jwts.builder().setSubject(email).claim("ip", ip).claim("userAgent", userAgent)
				.signWith(Keys.hmacShaKeyFor(SECRET_ADMIN), SignatureAlgorithm.HS256).compact();
	}

//	public Boolean checkpassword(String email, String password) {
//		Boolean flag=userRepository.checkPassword(email, password);
//		if(flag)return true;
//		else throw new IllegalArgumentException("Invalid email or password");
//	}
	
	public Boolean checkpassword(String email, String password) {
		
	    String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
	    Pattern pattern = Pattern.compile(emailRegex);

	    if (email == null || !pattern.matcher(email).matches()) {
	        throw new IllegalArgumentException("Invalid email format");
	    }

	    Boolean flag = userRepository.checkPassword(email, password);

	    if (Boolean.TRUE.equals(flag)) {
	        return true;
	    } else {
	        throw new IllegalArgumentException("Invalid email or password");
	    }
	}
	
	public Boolean checkpasswordAdmin(String email, String password) {
		Boolean flag=userRepository.checkPasswordAdmin(email, password);
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
	
	public String getEmailFromTokenAdmin(String jwt, String ip, String userAgent) {
		if (jwt == null)
			return null;
		try {
			Claims claims = Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(SECRET_ADMIN)).build().parseClaimsJws(jwt)
					.getBody();

			String ipClaim = (String) claims.get("ip");
			String userAgentClaim = (String) claims.get("userAgent");
			if (ip != null && ip.equals(ipClaim) && userAgent != null && userAgent.equals(userAgentClaim)
					&& userRepository.doesEmailExistAdmin(claims.getSubject())) {
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
	
	public Boolean checkTokenValidityAdmin(String jwt, String ip, String userAgent) {
		if (jwt == null)
			return false;
		try {
			Claims claims = Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(SECRET_ADMIN)).build().parseClaimsJws(jwt)
					.getBody();

			String ipClaim = (String) claims.get("ip");
			String userAgentClaim = (String) claims.get("userAgent");
			if (ip != null && ip.equals(ipClaim) && userAgent != null && userAgent.equals(userAgentClaim)
					&& userRepository.doesEmailExistAdmin(claims.getSubject())) {
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
				if(checkpasswordAdmin(claims.getSubject(), currentPassword)) {
					throw new IllegalArgumentException("It is not possible to change the admin password from this site!");
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
	
	public String ChangeAdminPassword(String jwt,String currentPassword, String password, String confirmPassword, String ip, String userAgent) {
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
					userRepository.updateAdminPassword(claims.getSubject(), password);
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

	public String getWebSocketUUIDByEmail(String email) {
		return userRepository.getWebSocketUUIDByEmail(email);
	}


}
