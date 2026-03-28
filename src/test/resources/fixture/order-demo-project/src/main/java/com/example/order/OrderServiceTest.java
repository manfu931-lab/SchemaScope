package com.example.order;

public class OrderServiceTest {

    public void shouldListOrdersByStatus() {
        OrderService service = null;
        service.listByStatus("PAID");
    }
}