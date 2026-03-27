package com.example.demo;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OwnerService {

    private final OwnerRepository ownerRepository;

    public OwnerService(OwnerRepository ownerRepository) {
        this.ownerRepository = ownerRepository;
    }

    public List<Object> searchByLastName(String lastName) {
        return ownerRepository.findByLastName(lastName);
    }
}