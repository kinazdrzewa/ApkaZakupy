package com.example.app.controller;

import com.example.app.model.Product;
import com.example.app.repository.ProductRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*") // pozwala na połączenie z aplikacji Kotlin
public class ProductController {

    private final ProductRepository productRepo;

    public ProductController(ProductRepository productRepo) {
        this.productRepo = productRepo;
    }

    @GetMapping
    public List<Product> getAllProducts() {
        return productRepo.findAll();
    }

    @GetMapping("/{barcode}")
    public Optional<Product> getProductByBarcode(@PathVariable String barcode) {
        return productRepo.findByBarcode(barcode);
    }

    @PostMapping
    public Product addProduct(@RequestBody Product product) {
        return productRepo.save(product);
    }

    @PutMapping("/{id}")
    public Product updateProduct(@PathVariable Long id, @RequestBody Product product) {
        return productRepo.findById(id).map(existing -> {
            existing.setName(product.getName());
            existing.setBarcode(product.getBarcode());
            existing.setCalories(product.getCalories());
            existing.setProtein(product.getProtein());
            existing.setFat(product.getFat());
            existing.setCarbohydrates(product.getCarbohydrates());
            return productRepo.save(existing);
        }).orElseGet(() -> {
            product.setId(id);
            return productRepo.save(product);
        });
    }

    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable Long id) {
        productRepo.deleteById(id);
    }
}
