package com.example.app.model;

import jakarta.persistence.*;

@Entity
@Table(name = "shopping_list_items")
public class ShoppingListItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int quantity;

    @ManyToOne
    @JoinColumn(name = "list_id")
    private ShoppingList shoppingList;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    // Gettery i settery
}
