package com.example.order;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository {

    @Query(value = "select * from orders where status = :status", nativeQuery = true)
    List<Object> findByStatus(String status);
}