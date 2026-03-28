 package com.example.order;

public class OrderJdbcDaoTest {

    public void shouldUpdateOrderStatus() {
        OrderJdbcDao dao = null;
        dao.updateOrderStatus(1L, "PAID");
    }
}