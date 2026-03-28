package com.example.demo;

import org.springframework.stereotype.Repository;

@Repository
public class VisitJdbcDao {

    public void findVisitsByPetId(Long petId) {
        String sql = "select id, pet_id "
                + "from visits "
                + "where pet_id = ?";
        jdbcTemplate.query(sql);
    }

    public void findVisitDates() {
        StringBuilder sql = new StringBuilder();
        sql.append("select visit_date ");
        sql.append("from visits ");
        sql.append("where description is not null");
        jdbcTemplate.query(sql.toString());
    }
}