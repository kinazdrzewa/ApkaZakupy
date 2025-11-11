package com.example.app.model;

import jakarta.persistence.*;

@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String barcode;
    private Double calories;
    private Double protein;
    private Double fat;
    private Double carbohydrates;

    // Gettery i settery
}