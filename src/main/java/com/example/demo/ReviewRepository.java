package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class ReviewRepository {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    public Long insertReview(String productId, String userEmail, String reviewText) {
        String sql = "INSERT INTO reviews (product_id, user_email, review_text, created_at) " +
                     "VALUES (:productId, :userEmail, :reviewText, CURRENT_TIMESTAMP)";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("productId", productId)
                .addValue("userEmail", userEmail)
                .addValue("reviewText", reviewText);

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(sql, params, keyHolder, new String[]{"id"});
        return keyHolder.getKey().longValue();
    }

    public Long insertRating(String productId, String userEmail, int rating) {
    	
        String sql = "INSERT INTO ratings (product_id, user_email, rating, created_at) " +
                     "VALUES (:productId, :userEmail, :rating, CURRENT_TIMESTAMP)";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("productId", productId)
                .addValue("userEmail", userEmail)
                .addValue("rating", rating);

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(sql, params, keyHolder, new String[]{"id"});
        
        insertRating(productId,  findProductRating(productId));
        return keyHolder.getKey().longValue();
    }
    

    public boolean updateRating(String productId, String userEmail, int newRating) {
        String sql = "UPDATE ratings SET rating = :rating WHERE product_id = :productId AND user_email = :userEmail";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("productId", productId)
                .addValue("userEmail", userEmail)
                .addValue("rating", newRating);
        int rows = jdbcTemplate.update(sql, params);
        insertRating(productId,  findProductRating(productId));
        return rows > 0;
    }

    public boolean deleteRating(String productId, String userEmail) {
        String sql = "DELETE FROM ratings WHERE product_id = :productId AND user_email = :userEmail";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("productId", productId)
                .addValue("userEmail", userEmail);
        int rows = jdbcTemplate.update(sql, params);
        insertRating(productId,  findProductRating(productId));
        return rows > 0;
    }

    public boolean hasRated(String productId, String userEmail) {
        String sql = "SELECT COUNT(*) FROM ratings WHERE product_id = :productId AND user_email = :userEmail";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("productId", productId)
                .addValue("userEmail", userEmail);
        Integer count = jdbcTemplate.queryForObject(sql, params, Integer.class);
        return count != null && count > 0;
    }

    public boolean deleteReview(Long reviewId, String userEmail) {
        String deleteVotesSql = "DELETE FROM review_votes WHERE review_id = :reviewId";
        MapSqlParameterSource voteParams = new MapSqlParameterSource()
                .addValue("reviewId", reviewId);
        jdbcTemplate.update(deleteVotesSql, voteParams);

        String deleteReviewSql = "DELETE FROM reviews WHERE id = :id AND user_email = :userEmail";
        MapSqlParameterSource reviewParams = new MapSqlParameterSource()
                .addValue("id", reviewId)
                .addValue("userEmail", userEmail);
        int rows = jdbcTemplate.update(deleteReviewSql, reviewParams);
        return rows > 0;
    }

    public List<Review> getReviewsForProduct(String productId, String sortBy, String currentUserEmail) {
        String orderBy;
        switch (sortBy) {
            case "helpful":
                orderBy = "like_count DESC, created_at DESC";
                break;
            case "latest":
            default:
                orderBy = "created_at DESC";
                break;
        }

        String sql = "SELECT r.id, r.product_id, r.user_email, u.name AS user_name, r.review_text, r.created_at, " +
                     "COALESCE(like_votes.count, 0) AS like_count, COALESCE(dislike_votes.count, 0) AS dislike_count, " +
                     "user_vote.vote_type AS user_vote " +
                     "FROM reviews r " +
                     "JOIN users u ON r.user_email = u.email " +
                     "LEFT JOIN (SELECT review_id, COUNT(*) AS count FROM review_votes WHERE vote_type = 'like' GROUP BY review_id) like_votes ON r.id = like_votes.review_id " +
                     "LEFT JOIN (SELECT review_id, COUNT(*) AS count FROM review_votes WHERE vote_type = 'dislike' GROUP BY review_id) dislike_votes ON r.id = dislike_votes.review_id " +
                     "LEFT JOIN (SELECT review_id, vote_type FROM review_votes WHERE user_email = :currentUserEmail) user_vote ON r.id = user_vote.review_id " +
                     "WHERE r.product_id = :productId " +
                     "ORDER BY " + orderBy;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("productId", productId)
                .addValue("currentUserEmail", currentUserEmail);

        return jdbcTemplate.query(sql, params, this::mapToReview);
    }

    public List<Rating> getRatingsForProduct(String productId) {
        String sql = "SELECT id, product_id, user_email, rating, created_at FROM ratings WHERE product_id = :productId";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("productId", productId);
        return jdbcTemplate.query(sql, params, this::mapToRating);
    }

    public void upsertVote(Long reviewId, String userEmail, String voteType) {
        String checkSql = "SELECT vote_type FROM review_votes WHERE review_id = :reviewId AND user_email = :userEmail";
        MapSqlParameterSource checkParams = new MapSqlParameterSource()
                .addValue("reviewId", reviewId)
                .addValue("userEmail", userEmail);

        List<String> existing = jdbcTemplate.queryForList(checkSql, checkParams, String.class);

        if (!existing.isEmpty()) {
            String currentVote = existing.get(0);
            if (currentVote.equals(voteType)) {
                String deleteSql = "DELETE FROM review_votes WHERE review_id = :reviewId AND user_email = :userEmail";
                jdbcTemplate.update(deleteSql, checkParams);
            } else {
                String updateSql = "UPDATE review_votes SET vote_type = :voteType WHERE review_id = :reviewId AND user_email = :userEmail";
                MapSqlParameterSource updateParams = new MapSqlParameterSource()
                        .addValue("reviewId", reviewId)
                        .addValue("userEmail", userEmail)
                        .addValue("voteType", voteType);
                jdbcTemplate.update(updateSql, updateParams);
            }
        } else {
            String insertSql = "INSERT INTO review_votes (review_id, user_email, vote_type, created_at) " +
                              "VALUES (:reviewId, :userEmail, :voteType, CURRENT_TIMESTAMP)";
            MapSqlParameterSource insertParams = new MapSqlParameterSource()
                    .addValue("reviewId", reviewId)
                    .addValue("userEmail", userEmail)
                    .addValue("voteType", voteType);
            jdbcTemplate.update(insertSql, insertParams);
        }
    }

    public boolean reviewExistsForProduct(Long reviewId, String productId) {
        String sql = "SELECT COUNT(*) FROM reviews WHERE id = :id AND product_id = :productId";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", reviewId)
                .addValue("productId", productId);
        Integer count = jdbcTemplate.queryForObject(sql, params, Integer.class);
        return count != null && count > 0;
    }

    private Review mapToReview(ResultSet rs, int rowNum) throws SQLException {
        return Review.builder()
                .id(rs.getLong("id"))
                .productId(rs.getString("product_id"))
                .userEmail(rs.getString("user_email"))
                .userName(rs.getString("user_name"))
                .reviewText(rs.getString("review_text"))
                .createdAt(rs.getTimestamp("created_at"))
                .likeCount(rs.getInt("like_count"))
                .dislikeCount(rs.getInt("dislike_count"))
                .userVote(rs.getString("user_vote"))
                .build();
    }

    private Rating mapToRating(ResultSet rs, int rowNum) throws SQLException {
        return Rating.builder()
                .id(rs.getLong("id"))
                .productId(rs.getString("product_id"))
                .userEmail(rs.getString("user_email"))
                .rating(rs.getInt("rating"))
                .createdAt(rs.getTimestamp("created_at"))
                .build();
    }
    public double findProductRating(String productId) {
	    String sql = "SELECT AVG(rating) FROM ratings WHERE product_id = :product_id";
	    MapSqlParameterSource params = new MapSqlParameterSource();
	    params.addValue("product_id", productId);
	    try {
	        Double result = jdbcTemplate.queryForObject(sql, params, Double.class);
	        return result != null ? result : 0.0;
	    } catch (Exception e) {
	        return 0.0;
	    }
	}
    public void insertRating(String id,double rating) {
    	String sql="UPDATE product set rating = :rating WHERE id = :id";
    	MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("rating", rating);
    	jdbcTemplate.update(sql, params);
    }
}