package com.example.demo;

public class OwnerJdbcDaoTest {

    public void shouldUpdateOwnerLastName() {
        OwnerJdbcDao dao = null;
        dao.updateOwnerLastName(1L, "Davis");
    }
}