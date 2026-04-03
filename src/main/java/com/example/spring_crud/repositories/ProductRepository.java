package com.example.spring_crud.services;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.spring_crud.models.Product;

public interface ProductRepository extends JpaRepository<Product, Integer> {

}
