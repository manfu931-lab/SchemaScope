package com.example.order;

public class OrderControllerTest {

    public void shouldListOrdersByStatus() {
        OrderController controller = null;
        controller.listOrders("PAID");
    }
}