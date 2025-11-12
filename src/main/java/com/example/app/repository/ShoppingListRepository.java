package com.example.app.repository;

import com.example.app.model.ShoppingList;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ShoppingListRepository extends JpaRepository<ShoppingList, Long> {
    Optional<ShoppingList> findByNameAndUserId(String name, Long userId);
    List<ShoppingList> findAllByUserId(Long userId);
}
