package com.example.demo;

import org.springframework.stereotype.Repository;

@Repository
public class OwnerJdbcDao {

    public void updateOwnerLastName(Long id, String lastName) {
        jdbcTemplate.update("update owners set last_name = ? where id = ?");
    }
}