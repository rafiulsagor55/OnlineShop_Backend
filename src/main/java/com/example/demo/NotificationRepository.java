package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class NotificationRepository {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public void insertNotification(Notification notification) {
        String sql = "INSERT INTO notifications (id, email, title, message, timestamp, `read`, type, serialId) " +
                     "VALUES (:id, :email, :title, :message, :timestamp, :read, :type, :serialId)";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", notification.getId())
                .addValue("email", notification.getEmail())
                .addValue("title", notification.getTitle())
                .addValue("message", notification.getMessage())
                .addValue("timestamp", notification.getTimestamp())
                .addValue("read", notification.isRead())
                .addValue("type", notification.getType())
                .addValue("serialId", notification.getSerialId());

        jdbcTemplate.update(sql, params);
    }
    
    public void insertActivityLog(ActivityLog activityLog) {
        String sql = "INSERT INTO ActivityLog (id, title, message, timestamp, type) " +
                     "VALUES (:id, :title, :message, :timestamp, :type)";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", activityLog.getId())
                .addValue("title", activityLog.getTitle())
                .addValue("message", activityLog.getMessage())
                .addValue("timestamp", activityLog.getTimestamp())
                .addValue("type", activityLog.getType());

        jdbcTemplate.update(sql, params);
    }

    public List<Notification> getNotificationsByEmail(String email) {
        String sql = "SELECT id, email, title, message, timestamp, `read`, type, serialId " +
                     "FROM notifications WHERE email = :email";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("email", email);

        return jdbcTemplate.query(sql, params, new BeanPropertyRowMapper<>(Notification.class));
    }
    
    public List<ActivityLog> getAllActivityLog() {
        String sql = "SELECT id, title, message, timestamp, type " +
                     "FROM ActivityLog";

        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(ActivityLog.class));
    }

    public void deleteNotificationById(String id) {
        String sql = "DELETE FROM notifications WHERE id = :id";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id);

        jdbcTemplate.update(sql, params);
    }
    
    public void readNotificationById(String id) {
        String sql = "UPDATE notifications SET `read` = :read WHERE id = :id";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("read", true);

        jdbcTemplate.update(sql, params);
    }

    public boolean isEmailValid(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = :email";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("email", email);
        Integer count = jdbcTemplate.queryForObject(sql, params, Integer.class);
        return count != null && count > 0;
    }
    
    public void updateNotificationCount(String email, int count) {
        String sql = "INSERT INTO newItemsCounter (email, notifications) " +
                     "VALUES (:email, :notifications) " +
                     "ON DUPLICATE KEY UPDATE notifications = :notifications";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("email", email)
                .addValue("notifications", count);

        jdbcTemplate.update(sql, params);
    }

    public int getNotificationCount(String email) {
        String sql = "SELECT notifications FROM newItemsCounter WHERE email = :email";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("email", email);

        try {
            Integer count = jdbcTemplate.queryForObject(sql, params, Integer.class);
            return count != null ? count : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    
}