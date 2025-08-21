package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class FilterRepository {

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public void deleteByTypeAndGender(String filterType, String gender) {
        String sql = "DELETE FROM product_filters WHERE filter_type = :filterType AND gender = :gender";
        Map<String, Object> params = new HashMap<>();
        params.put("filterType", filterType);
        params.put("gender", gender);
        namedParameterJdbcTemplate.update(sql, params);
    }

    public void insertBatch(String filterType, List<String> values, String gender) {
        if (values == null || values.isEmpty()) {
            return;
        }

        String sql = "INSERT INTO product_filters (filter_type, filter_value, gender) VALUES (:filterType, :filterValue, :gender)";
        List<SqlParameterSource> batchParams = new ArrayList<>();

        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                Map<String, Object> params = new HashMap<>();
                params.put("filterType", filterType);
                params.put("filterValue", value.trim());
                params.put("gender", gender);
                batchParams.add(new MapSqlParameterSource(params));
            }
        }

        if (!batchParams.isEmpty()) {
            namedParameterJdbcTemplate.batchUpdate(sql, batchParams.toArray(new SqlParameterSource[0]));
        }
    }

    public Map<String, List<String>> getAllFilters(String gender) {
        String sql = "SELECT filter_type, filter_value FROM product_filters WHERE gender = :gender ORDER BY filter_type, filter_value";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("gender", gender);
        List<Map<String, Object>> rows = namedParameterJdbcTemplate.queryForList(sql, params);
        System.out.println("Fetched rows for gender " + gender + ": " + rows);

        Map<String, List<String>> filters = new HashMap<>();
        for (Map<String, Object> row : rows) {
            String filterType = (String) row.get("filter_type");
            String filterValue = (String) row.get("filter_value");
            filters.computeIfAbsent(filterType, k -> new ArrayList<>()).add(filterValue);
        }

        return filters;
    }
}