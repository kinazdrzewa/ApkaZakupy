package com.example.app.model;

import jakarta.persistence.*;

@Entity
@Table(name = "shopping_list", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "name"})
})
public class ShoppingList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String name;

    public ShoppingList() {}

    public ShoppingList(Long userId, String name) {
        this.userId = userId;
        this.name = name;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
