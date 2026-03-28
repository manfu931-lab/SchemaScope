package com.example.order;

import org.springframework.stereotype.Repository;

@Repository
public class OrderJdbcDao {

    public void updateOrderStatus(Long id, String status) {
        jdbcTemplate.update("update orders set status = ? where id = ?", status, id);
    }
}