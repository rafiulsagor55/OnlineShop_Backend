package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

@Repository
public class QARepository {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    // Insert a new question
    public Long insertQuestion(String productId, String userEmail, String question) {
        String sql = "INSERT INTO questions (product_id, user_email, question, created_at) VALUES (:productId, :userEmail, :question, CURRENT_TIMESTAMP)";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("productId", productId)
                .addValue("userEmail", userEmail)
                .addValue("question", question);

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(sql, params, keyHolder, new String[]{"id"});
        return keyHolder.getKey().longValue();
    }

    // Insert a new answer
    public Long insertAnswer(Long questionId, String userEmail, String answer) {
        String sql = "INSERT INTO answers (question_id, user_email, answer, created_at) VALUES (:questionId, :userEmail, :answer, CURRENT_TIMESTAMP)";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("questionId", questionId)
                .addValue("userEmail", userEmail)
                .addValue("answer", answer);

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(sql, params, keyHolder, new String[]{"id"});
        return keyHolder.getKey().longValue();
    }

    // Delete question by ID if owned by user, including associated answers and votes
    public boolean deleteQuestion(Long questionId, String userEmail) {
        // Delete associated votes for the question
        String deleteQuestionVotesSql = "DELETE FROM votes WHERE entity_type = 'question' AND entity_id = :questionId";
        MapSqlParameterSource voteParams = new MapSqlParameterSource()
                .addValue("questionId", questionId);
        jdbcTemplate.update(deleteQuestionVotesSql, voteParams);

        // Delete associated votes for answers to this question
        String deleteAnswerVotesSql = "DELETE FROM votes WHERE entity_type = 'answer' AND entity_id IN (SELECT id FROM answers WHERE question_id = :questionId)";
        jdbcTemplate.update(deleteAnswerVotesSql, voteParams);

        // Delete associated answers
        String deleteAnswersSql = "DELETE FROM answers WHERE question_id = :questionId";
        jdbcTemplate.update(deleteAnswersSql, voteParams);

        // Delete the question if owned by the user
        String deleteQuestionSql = "DELETE FROM questions WHERE id = :id AND user_email = :userEmail";
        MapSqlParameterSource questionParams = new MapSqlParameterSource()
                .addValue("id", questionId)
                .addValue("userEmail", userEmail);
        int rows = jdbcTemplate.update(deleteQuestionSql, questionParams);
        return rows > 0;
    }

    // Delete answer by ID if owned by user
    public boolean deleteAnswer(Long answerId, String userEmail) {
        // Delete associated votes for the answer
        String deleteVotesSql = "DELETE FROM votes WHERE entity_type = 'answer' AND entity_id = :answerId";
        MapSqlParameterSource voteParams = new MapSqlParameterSource()
                .addValue("answerId", answerId);
        jdbcTemplate.update(deleteVotesSql, voteParams);

        // Delete the answer if owned by the user
        String deleteAnswerSql = "DELETE FROM answers WHERE id = :id AND user_email = :userEmail";
        MapSqlParameterSource answerParams = new MapSqlParameterSource()
                .addValue("id", answerId)
                .addValue("userEmail", userEmail);
        int rows = jdbcTemplate.update(deleteAnswerSql, answerParams);
        return rows > 0;
    }

    // Get questions for product, sorted and filtered
    public List<Question> getQuestionsForProduct(String productId, String sortBy, String filter, String currentUserEmail) {
        // Determine sorting order: 'helpful' sorts by like_count DESC, then created_at DESC; otherwise, sort by created_at DESC
        String orderBy = sortBy.equals("helpful") ? "like_count DESC, created_at DESC" : "created_at DESC";
        // Apply filter: 'answered' for questions with answers, 'unanswered' for questions without answers, 'all' for no filter
        String whereFilter = "";
        if (filter.equals("answered")) {
            whereFilter = " AND answer_count > 0";
        } else if (filter.equals("unanswered")) {
            whereFilter = " AND answer_count = 0";
        }

        String sql = "SELECT q.id, q.product_id, q.user_email, u.name AS user_name, q.question, q.created_at, " +
                "COALESCE(like_votes.count, 0) AS like_count, COALESCE(dislike_votes.count, 0) AS dislike_count, " +
                "user_vote.vote_type AS user_vote, " +
                "COALESCE(answer_count.count, 0) AS answer_count " +
                "FROM questions q " +
                "JOIN users u ON q.user_email = u.email " +
                "LEFT JOIN (SELECT entity_id, COUNT(*) AS count FROM votes WHERE entity_type = 'question' AND vote_type = 'like' GROUP BY entity_id) like_votes ON q.id = like_votes.entity_id " +
                "LEFT JOIN (SELECT entity_id, COUNT(*) AS count FROM votes WHERE entity_type = 'question' AND vote_type = 'dislike' GROUP BY entity_id) dislike_votes ON q.id = dislike_votes.entity_id " +
                "LEFT JOIN (SELECT entity_id, vote_type FROM votes WHERE entity_type = 'question' AND user_email = :currentUserEmail) user_vote ON q.id = user_vote.entity_id " +
                "LEFT JOIN (SELECT question_id, COUNT(*) AS count FROM answers GROUP BY question_id) answer_count ON q.id = answer_count.question_id " +
                "WHERE q.product_id = :productId " + whereFilter +
                " ORDER BY " + orderBy;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("productId", productId)
                .addValue("currentUserEmail", currentUserEmail);

        return jdbcTemplate.query(sql, params, this::mapToQuestion);
    }

    // Get answers for question
    public List<Answer> getAnswersForQuestion(Long questionId, String currentUserEmail) {
        String sql = "SELECT a.id, a.question_id, a.user_email, u.name AS user_name, a.answer, a.created_at, " +
                "COALESCE(like_votes.count, 0) AS like_count, COALESCE(dislike_votes.count, 0) AS dislike_count, " +
                "user_vote.vote_type AS user_vote " +
                "FROM answers a " +
                "JOIN users u ON a.user_email = u.email " +
                "LEFT JOIN (SELECT entity_id, COUNT(*) AS count FROM votes WHERE entity_type = 'answer' AND vote_type = 'like' GROUP BY entity_id) like_votes ON a.id = like_votes.entity_id " +
                "LEFT JOIN (SELECT entity_id, COUNT(*) AS count FROM votes WHERE entity_type = 'answer' AND vote_type = 'dislike' GROUP BY entity_id) dislike_votes ON a.id = dislike_votes.entity_id " +
                "LEFT JOIN (SELECT entity_id, vote_type FROM votes WHERE entity_type = 'answer' AND user_email = :currentUserEmail) user_vote ON a.id = user_vote.entity_id " +
                "WHERE a.question_id = :questionId " +
                "ORDER BY a.created_at ASC";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("questionId", questionId)
                .addValue("currentUserEmail", currentUserEmail);

        return jdbcTemplate.query(sql, params, this::mapToAnswer);
    }

    // Insert or update vote
    public void upsertVote(String entityType, Long entityId, String userEmail, String voteType) {
        // First, check existing vote
        String checkSql = "SELECT vote_type FROM votes WHERE entity_type = :entityType AND entity_id = :entityId AND user_email = :userEmail";
        MapSqlParameterSource checkParams = new MapSqlParameterSource()
                .addValue("entityType", entityType)
                .addValue("entityId", entityId)
                .addValue("userEmail", userEmail);

        List<String> existing = jdbcTemplate.queryForList(checkSql, checkParams, String.class);

        if (!existing.isEmpty()) {
            String currentVote = existing.get(0);
            if (currentVote.equals(voteType)) {
                // Same vote, remove it
                String deleteSql = "DELETE FROM votes WHERE entity_type = :entityType AND entity_id = :entityId AND user_email = :userEmail";
                jdbcTemplate.update(deleteSql, checkParams);
            } else {
                // Different vote, update it
                String updateSql = "UPDATE votes SET vote_type = :voteType WHERE entity_type = :entityType AND entity_id = :entityId AND user_email = :userEmail";
                MapSqlParameterSource updateParams = new MapSqlParameterSource()
                        .addValue("entityType", entityType)
                        .addValue("entityId", entityId)
                        .addValue("userEmail", userEmail)
                        .addValue("voteType", voteType);
                jdbcTemplate.update(updateSql, updateParams);
            }
        } else {
            // No vote exists, insert new
            String insertSql = "INSERT INTO votes (entity_type, entity_id, user_email, vote_type, created_at) VALUES (:entityType, :entityId, :userEmail, :voteType, CURRENT_TIMESTAMP)";
            MapSqlParameterSource insertParams = new MapSqlParameterSource()
                    .addValue("entityType", entityType)
                    .addValue("entityId", entityId)
                    .addValue("userEmail", userEmail)
                    .addValue("voteType", voteType);
            jdbcTemplate.update(insertSql, insertParams);
        }
    }

    // Mappers
    private Question mapToQuestion(ResultSet rs, int rowNum) throws SQLException {
        return Question.builder()
                .id(rs.getLong("id"))
                .productId(rs.getString("product_id"))
                .userEmail(rs.getString("user_email"))
                .userName(rs.getString("user_name"))
                .question(rs.getString("question"))
                .createdAt(rs.getTimestamp("created_at"))
                .likeCount(rs.getInt("like_count"))
                .dislikeCount(rs.getInt("dislike_count"))
                .userVote(rs.getString("user_vote"))
                .build();
    }

    private Answer mapToAnswer(ResultSet rs, int rowNum) throws SQLException {
        return Answer.builder()
                .id(rs.getLong("id"))
                .questionId(rs.getLong("question_id"))
                .userEmail(rs.getString("user_email"))
                .userName(rs.getString("user_name"))
                .answer(rs.getString("answer"))
                .createdAt(rs.getTimestamp("created_at"))
                .likeCount(rs.getInt("like_count"))
                .dislikeCount(rs.getInt("dislike_count"))
                .userVote(rs.getString("user_vote"))
                .build();
    }

    // Check if question exists and belongs to product
    public boolean questionExistsForProduct(Long questionId, String productId) {
        String sql = "SELECT COUNT(*) FROM questions WHERE id = :id AND product_id = :productId";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", questionId)
                .addValue("productId", productId);
        Integer count = jdbcTemplate.queryForObject(sql, params, Integer.class);
        return count != null && count > 0;
    }
}