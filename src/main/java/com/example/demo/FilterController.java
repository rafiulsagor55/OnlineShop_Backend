package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RequestMapping("/api/filters")
public class FilterController {

    @Autowired
    private FilterService filterService;

    @Autowired
    private UserService userService;
    @Autowired
    private NotificationService notificationService;

    @PostMapping("/save-filters")
    public ResponseEntity<String> saveFilters(
            @RequestBody Map<String, List<String>> filters,
            @CookieValue(name = "admin_token", required = false) String jwt,
            HttpServletRequest request) {
        try {
            String userAgent = request.getHeader("User-Agent");
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null) {
                ip = request.getRemoteAddr();
            }
            System.out.println("IP: "+ip);
//            System.out.println("Received filters payload: " + filters); // Debug payload
            if (!userService.checkTokenValidityAdmin(jwt, ip, userAgent)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Your session has expired or the token is invalid. Please log in again to continue.");
            }
            filterService.saveFilters(filters);
            notificationService.createActivityLog(ActivityLog.builder()
			        .id(UUID.randomUUID().toString())
			        .title(String.format("Filter option updated"))
			        .message(String.format("Filter option of  \"%s\" has been successfully updated.",filters.get("gender").get(0)))
			        .timestamp(new Timestamp(System.currentTimeMillis()))
			        .type("filter")
			        .build());
            return ResponseEntity.ok("Filters saved successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while saving filters: " + e.getMessage());
        }
    }

    @GetMapping("/get-filters")
    public ResponseEntity<Map<String, List<String>>> getAllFilters(@RequestParam String gender) {
        try {
            Map<String, List<String>> filters = filterService.getAllFilters(gender);
            return ResponseEntity.ok(filters);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
}