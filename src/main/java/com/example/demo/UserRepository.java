package com.example.demo;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {
	@Autowired
	private NamedParameterJdbcTemplate jdbcTemplate;

	public boolean doesEmailExistWithPassword(String email) {
		String CHECK_EMAIL_EXISTS = "SELECT COUNT(*) FROM passwords WHERE email = :email";
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("email", email);

		int count = jdbcTemplate.queryForObject(CHECK_EMAIL_EXISTS, params, Integer.class);

		return count > 0;
	}

	public boolean checkPassword(String email, String password) {
		// Correct the query by removing the extra space after :password
		String CHECK_EMAIL_EXISTS = "SELECT COUNT(*) FROM passwords WHERE email = :email AND password = :password";

		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("email", email);
		params.addValue("password", password);

		// Execute the query correctly using named parameters
		int count = jdbcTemplate.queryForObject(CHECK_EMAIL_EXISTS, params, Integer.class);
		return count > 0;
	}

	public boolean doesEmailExist(String email) {
		String CHECK_EMAIL_EXISTS = "SELECT COUNT(*) FROM users WHERE email = :email";
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("email", email);

		int count = jdbcTemplate.queryForObject(CHECK_EMAIL_EXISTS, params, Integer.class);

		return count > 0;
	}

	public void insertCode(String email, String code) {
		String INSERT_CODE_SQL = "INSERT INTO codes (email, code) " + "VALUES (:email, :code)";
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("email", email);
		params.addValue("code", code);

		jdbcTemplate.update(INSERT_CODE_SQL, params);
	}

	public void deleteByEmail(String email) {
		String DELETE_BY_EMAIL_SQL = "DELETE FROM codes WHERE email = :email";
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("email", email);
		jdbcTemplate.update(DELETE_BY_EMAIL_SQL, params);
	}

	public boolean doesCodeExistForEmail(String email, String code) {
		String CHECK_EMAIL_AND_CODE_SQL = "SELECT COUNT(*) FROM codes WHERE email = :email AND code = :code";
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("email", email);
		params.addValue("code", code);

		Integer count = jdbcTemplate.queryForObject(CHECK_EMAIL_AND_CODE_SQL, params, Integer.class);

		return count != null && count > 0;
	}

	public void incrementCount(String email) {
		String INCREMENT_COUNT_SQL = "UPDATE codes SET count = count + 1 WHERE email = :email";
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("email", email);
		jdbcTemplate.update(INCREMENT_COUNT_SQL, params);
	}

	public int getCount(String email) {
		String GET_COUNT_SQL = "SELECT count FROM codes WHERE email = :email";
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("email", email);
		try {
			Integer count = jdbcTemplate.queryForObject(GET_COUNT_SQL, params, Integer.class);
			return count != null ? count : 0;
		} catch (Exception e) {
			return -1;
		}
	}

	public void insertPassword(String email, String password) {
		String INSERT_CODE_SQL = "INSERT INTO passwords (email, password) " + "VALUES (:email, :password)";
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("email", email);
		params.addValue("password", password);

		jdbcTemplate.update(INSERT_CODE_SQL, params);
	}

	public void insertUserDetails(String email, String name, byte[] image_data, String content_type) {
		String INSERT_CODE_SQL = "INSERT INTO users (email, name, image_data,content_type) " + "VALUES (:email, :name, :image_data, :content_type)";
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("email", email);
		params.addValue("name", name);
		params.addValue("image_data", image_data);
		params.addValue("content_type", content_type);
		
		jdbcTemplate.update(INSERT_CODE_SQL, params);
	}

	public void insertUserDetailsFromGoogleLogin(String email, String name, String image_path) {
		String INSERT_CODE_SQL = "INSERT INTO users (email, name, image_path) " + "VALUES (:email, :name, :image_data)";
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("email", email);
		params.addValue("name", name);
		params.addValue("image_path", image_path);
		jdbcTemplate.update(INSERT_CODE_SQL, params);
	}

	public void insertUserDetailsFromGoogleLoginWithoutImage(String email, String name) {
		String INSERT_CODE_SQL = "INSERT INTO users (email, name) " + "VALUES (:email, :name)";
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("email", email);
		params.addValue("name", name);
		jdbcTemplate.update(INSERT_CODE_SQL, params);
	}

	public void updateUserDetailsFromGoogleLogin(String email, String name, String image_path) {
		String UPDATE_CODE_SQL = "UPDATE users SET name = :name, image_path = :image_path WHERE email = :email";
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("email", email);
		params.addValue("name", name);
		params.addValue("image_path", image_path);

		jdbcTemplate.update(UPDATE_CODE_SQL, params);
	}
	
	public void updateUserDetails(String email, String name, byte[] image_data, String content_type) {
		String UPDATE_CODE_SQL = "UPDATE users SET name = :name, image_data = :image_data, content_type = :content_type WHERE email = :email";
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("email", email);
		params.addValue("name", name);
		params.addValue("image_data", image_data);
		params.addValue("content_type", content_type);

		jdbcTemplate.update(UPDATE_CODE_SQL, params);
	}

	public void updatePassword(String email, String password) {
		String UPDATE_CODE_SQL = "UPDATE passwords SET password = :password WHERE email = :email";
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("email", email);
		params.addValue("password", password);

		jdbcTemplate.update(UPDATE_CODE_SQL, params);
	}

	public Users getUserDetailsByEmail(String email) {
	    String SQL = "SELECT email, name, "
	            + "image_path, "
	            + "CASE "
	            + "    WHEN content_type IS NOT NULL THEN image_data "
	            + "    ELSE NULL "
	            + "END AS image_data, "
	            + "content_type "
	            + "FROM users WHERE email = :email";

	    MapSqlParameterSource params = new MapSqlParameterSource();
	    params.addValue("email", email);

	    Users user = jdbcTemplate.queryForObject(SQL, params, this::mapRowToUser);

	    // Debugging logs
	    System.out.println("Fetched user: " + user.getEmail());
	    System.out.println("Image Path: " + user.getImagePath());
	    System.out.println("Image Data: " + (user.getImageData() != null ? "Has Data" : "No Data"));
	    System.out.println("Content Type: " + user.getContentType());

	    return user;
	}



	private Users mapRowToUser(ResultSet rs, int rowNum) throws SQLException {
	    Users user = new Users();
	    user.setEmail(rs.getString("email"));
	    user.setName(rs.getString("name"));
	    
	    // Always set image_path
	    user.setImagePath(rs.getString("image_path"));
	    
	    // Set image_data and content_type only if content_type is not NULL
	    if (rs.getString("content_type") != null) {
	        user.setImageData(rs.getBytes("image_data"));
	        user.setContentType(rs.getString("content_type"));
	    } else {
	        // If content_type is NULL, set image_data to NULL
	        user.setImageData(null);
	        user.setContentType(null);
	    }

	    return user;
	}


}
