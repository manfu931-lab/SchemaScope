package com.example.demo;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OwnerRepository {

    @Query(value = "select * from owners where last_name = :lastName", nativeQuery = true)
    List<Object> findByLastName(String lastName);
}