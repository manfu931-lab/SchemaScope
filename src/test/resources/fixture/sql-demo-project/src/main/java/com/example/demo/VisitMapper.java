package com.example.demo;

public interface VisitMapper {

    Object findVisitById(Long id);

    int updateVisitDescription(Long id, String description);
}