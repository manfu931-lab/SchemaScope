package com.example.demo;

public class OwnerRepositoryTest {

    public void shouldFindOwnersByLastName() {
        OwnerRepository repository = null;
        repository.findByLastName("Davis");
    }
}