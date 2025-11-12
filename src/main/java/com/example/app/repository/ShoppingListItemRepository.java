
package com.example.app.repository;

import com.example.app.model.ShoppingListItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ShoppingListItemRepository extends JpaRepository<ShoppingListItem, Long> {
    List<ShoppingListItem> findAllByShoppingListId(Long shoppingListId);
    ShoppingListItem findByShoppingListIdAndProductId(Long shoppingListId, Long productId);
}
