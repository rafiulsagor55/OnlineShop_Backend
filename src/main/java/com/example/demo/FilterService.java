package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class FilterService {

    @Autowired
    private FilterRepository filterRepository;

    @Transactional
    public void saveFilters(Map<String, List<String>> filters) {
        if (filters == null) {
            throw new IllegalArgumentException("Filters map cannot be null");
        }
        List<String> genderList = filters.get("gender");
        if (genderList == null || genderList.isEmpty()) {
            throw new IllegalArgumentException("Gender must be provided as a non-empty list");
        }
        String gender = genderList.get(0);
        if (gender == null || gender.trim().isEmpty()) {
            throw new IllegalArgumentException("Gender value cannot be empty");
        }
        System.out.println("Processing filters for gender: " + gender);

        for (Map.Entry<String, List<String>> entry : filters.entrySet()) {
            String filterType = entry.getKey();
            List<String> values = entry.getValue();
            if (!filterType.equals("gender")) {
                filterRepository.deleteByTypeAndGender(filterType, gender);
                filterRepository.insertBatch(filterType, values, gender);
            }
        }
    }

    public Map<String, List<String>> getAllFilters(String gender) {
        if (gender == null || gender.trim().isEmpty()) {
            throw new IllegalArgumentException("Gender must be provided");
        }
        return filterRepository.getAllFilters(gender);
    }
}