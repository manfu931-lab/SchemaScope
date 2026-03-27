package com.example.demo;

public class OwnerControllerTest {

    public void shouldReturnOwnersByLastName() {
        OwnerController controller = null;
        controller.listOwners("Davis");
    }
}