package com.example.app.model;

import jakarta.persistence.*;

@Entity
@Table(name = "shopping_list_item")
public class ShoppingListItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "shopping_list_id", nullable = false)
    private ShoppingList shoppingList;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity = 1;

    public ShoppingListItem() {
    }

    public ShoppingListItem(ShoppingList shoppingList, Product product, Integer quantity) {
        this.shoppingList = shoppingList;
        this.product = product;
        this.quantity = quantity == null ? 1 : quantity;
    }

    public Long getId() {
        return id;
    }

    public ShoppingList getShoppingList() {
        return shoppingList;
    }

    public void setShoppingList(ShoppingList shoppingList) {
        this.shoppingList = shoppingList;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity == null ? 1 : quantity;
    }
}
