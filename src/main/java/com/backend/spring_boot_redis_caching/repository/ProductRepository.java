package com.backend.spring_boot_redis_caching.repository;

import com.backend.spring_boot_redis_caching.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
}
