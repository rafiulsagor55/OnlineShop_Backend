package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    public void createNotification(Notification notification) {
        if (notification.getEmail() == null || notification.getEmail().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        if (!notificationRepository.isEmailValid(notification.getEmail())) {
            throw new IllegalArgumentException("Invalid email: " + notification.getEmail());
        }
        notificationRepository.insertNotification(notification);
    }
    
    public void createActivityLog(ActivityLog activityLog) {
        
        notificationRepository.insertActivityLog(activityLog);
    }


    public List<Notification> getNotificationsByEmail(String email) {
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        return notificationRepository.getNotificationsByEmail(email);
    }
    
    public List<ActivityLog> getActivityLog() {
        return notificationRepository.getAllActivityLog();
    }

    public void deleteNotification(String id) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Notification ID cannot be null or empty");
        }
        notificationRepository.deleteNotificationById(id);
    }
    
    public void readNotification(String id) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Notification ID cannot be null or empty");
        }
        notificationRepository.readNotificationById(id);
    }
    
    public void updateNotificationCount(String email, int count) {
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        notificationRepository.updateNotificationCount(email, count);
    }

    public int getNotificationCount(String email) {
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        return notificationRepository.getNotificationCount(email);
    }
}