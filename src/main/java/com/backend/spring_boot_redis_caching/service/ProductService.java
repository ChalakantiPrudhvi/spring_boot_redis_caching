
package com.backend.spring_boot_redis_caching.service;

import com.backend.spring_boot_redis_caching.entity.Product;
import com.backend.spring_boot_redis_caching.repository.ProductRepository;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // CREATE
    public Product createProduct(Product product) {
        product.setId(null);
        return productRepository.save(product);
    }

    // READ ALL
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // READ BY ID
    @Cacheable(value = "products", key = "#id")
    public Product getProductById(Long id) {
        System.out.println(
                "Fetching product from PostgreSQL: " + id);

        return productRepository.findById(id).orElse(null);
    }

    // UPDATE
    @CachePut(value = "products", key = "#id")
    public Product updateProduct(Long id, Product productDetails) {

        Product existingProduct =
                productRepository.findById(id).orElse(null);

        if (existingProduct == null) {
            return null;
        }

        existingProduct.setName(productDetails.getName());
        existingProduct.setDescription(productDetails.getDescription());
        existingProduct.setPrice(productDetails.getPrice());

        return productRepository.save(existingProduct);
    }

    // DELETE
    @CacheEvict(value = "products", key = "#id")
    public boolean deleteProduct(Long id) {

        if (!productRepository.existsById(id)) {
            return false;
        }

        productRepository.deleteById(id);
        return true;
    }
}
