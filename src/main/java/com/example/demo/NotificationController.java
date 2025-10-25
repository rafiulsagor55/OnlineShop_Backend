package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.CookieValue;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<?> createNotification(
            @RequestBody Notification notification,
            @CookieValue(name = "token", required = false) String jwt,
            HttpServletRequest request) {
        try {
            String userAgent = request.getHeader("User-Agent");
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null) {
                ip = request.getRemoteAddr();
            }
            if (userService.checkTokenValidity(jwt, ip, userAgent)) {
                notification.setEmail(userService.getEmailFromToken(jwt, ip, userAgent));
                notificationService.createNotification(notification);
                return ResponseEntity.ok("Notification created successfully");
            } else {
                throw new IllegalArgumentException("Your session has expired or the token is invalid. Please log in again to continue.");
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
//    @PostMapping("/activity-log")
//    public ResponseEntity<?> createActivityLog(
//            @RequestBody Notification notification,
//            @CookieValue(name = "admin_token", required = false) String jwt,
//            HttpServletRequest request) {
//        try {
//            String userAgent = request.getHeader("User-Agent");
//            String ip = request.getHeader("X-Forwarded-For");
//            if (ip == null) {
//                ip = request.getRemoteAddr();
//            }
//            if (userService.checkTokenValidity(jwt, ip, userAgent)) {
//                notification.setEmail(userService.getEmailFromToken(jwt, ip, userAgent));
//                notificationService.createNotification(notification);
//                return ResponseEntity.ok("Notification created successfully");
//            } else {
//                throw new IllegalArgumentException("Your session has expired or the token is invalid. Please log in again to continue.");
//            }
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.badRequest().body(e.getMessage());
//        }
//    }
    
    @PostMapping("/notificationCount")
    public ResponseEntity<?> updateNotificationCount(
            @RequestParam int count,
            @CookieValue(name = "token", required = false) String jwt,
            HttpServletRequest request) {
        try {
            String userAgent = request.getHeader("User-Agent");
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null) {
                ip = request.getRemoteAddr();
            }
            if (userService.checkTokenValidity(jwt, ip, userAgent)) {
                String email = userService.getEmailFromToken(jwt, ip, userAgent);
                notificationService.updateNotificationCount(email, count);
                return ResponseEntity.status(HttpStatus.OK).body(null);
            } else {
                throw new IllegalArgumentException("Your session has expired or the token is invalid. Please log in again to continue.");
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/count")
    public ResponseEntity<?> getNotificationCount(
            @CookieValue(name = "token", required = false) String jwt,
            HttpServletRequest request) {
        try {
            String userAgent = request.getHeader("User-Agent");
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null) {
                ip = request.getRemoteAddr();
            }
            if (userService.checkTokenValidity(jwt, ip, userAgent)) {
                String email = userService.getEmailFromToken(jwt, ip, userAgent);
                int count = notificationService.getNotificationCount(email);
                return ResponseEntity.ok(Map.of("count",count));
            } else {
                throw new IllegalArgumentException("Your session has expired or the token is invalid. Please log in again to continue.");
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getNotifications(
            @CookieValue(name = "token", required = false) String jwt,
            HttpServletRequest request) {
        try {
            String userAgent = request.getHeader("User-Agent");
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null) {
                ip = request.getRemoteAddr();
            }
            if (userService.checkTokenValidity(jwt, ip, userAgent)) {
                String email = userService.getEmailFromToken(jwt, ip, userAgent);
                List<Notification> notifications = notificationService.getNotificationsByEmail(email);
                return ResponseEntity.ok(notifications);
            } else {
                throw new IllegalArgumentException("Your session has expired or the token is invalid. Please log in again to continue.");
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @GetMapping("/activity-log")
    public ResponseEntity<?> getActivityLog(
            @CookieValue(name = "admin_token", required = false) String jwt,
            HttpServletRequest request) {
        try {
            String userAgent = request.getHeader("User-Agent");
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null) {
                ip = request.getRemoteAddr();
            }
            if (userService.checkTokenValidityAdmin(jwt, ip, userAgent)) {
                return ResponseEntity.ok(notificationService.getActivityLog());
            } else {
                throw new IllegalArgumentException("Your session has expired or the token is invalid. Please log in again to continue.");
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNotification(
            @PathVariable String id,
            @CookieValue(name = "token", required = false) String jwt,
            HttpServletRequest request) {
        try {
            String userAgent = request.getHeader("User-Agent");
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null) {
                ip = request.getRemoteAddr();
            }
            if (userService.checkTokenValidity(jwt, ip, userAgent)) {
                notificationService.deleteNotification(id);
                return ResponseEntity.ok("Notification deleted successfully");
            } else {
                throw new IllegalArgumentException("Your session has expired or the token is invalid. Please log in again to continue.");
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PostMapping("/{id}/read")
    public ResponseEntity<?> readNotification(
            @PathVariable String id,
            @CookieValue(name = "token", required = false) String jwt,
            HttpServletRequest request) {
        try {
            String userAgent = request.getHeader("User-Agent");
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null) {
                ip = request.getRemoteAddr();
            }
            if (userService.checkTokenValidity(jwt, ip, userAgent)) {
                notificationService.readNotification(id);
                return ResponseEntity.ok("Notification deleted successfully");
            } else {
                throw new IllegalArgumentException("Your session has expired or the token is invalid. Please log in again to continue.");
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    
}