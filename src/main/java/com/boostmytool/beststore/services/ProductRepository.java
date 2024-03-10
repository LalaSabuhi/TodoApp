package com.boostmytool.beststore.services;

import com.boostmytool.beststore.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Integer> {
}
