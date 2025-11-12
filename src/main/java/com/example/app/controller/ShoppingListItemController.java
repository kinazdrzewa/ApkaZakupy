// java
package com.example.app.controller;

import com.example.app.model.Product;
import com.example.app.model.ShoppingList;
import com.example.app.model.ShoppingListItem;
import com.example.app.repository.ProductRepository;
import com.example.app.repository.ShoppingListItemRepository;
import com.example.app.repository.ShoppingListRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/lists/{listId}/items")
@CrossOrigin(origins = "*")
public class ShoppingListItemController {
    private final ShoppingListRepository listRepo;
    private final ProductRepository productRepo;
    private final ShoppingListItemRepository itemRepo;

    public ShoppingListItemController(ShoppingListRepository listRepo,
                                      ProductRepository productRepo,
                                      @Qualifier("shoppingListItemRepository") ShoppingListItemRepository itemRepo) {
        this.listRepo = listRepo;
        this.productRepo = productRepo;
        this.itemRepo = itemRepo;
    }

    public static class ProductDto {
        // product fields
        public Long productId;
        public Long itemId; // <-- dodać itemId (shopping_list_item.id)
        public String name;
        public String barcode;
        public Double calories;
        public Double protein;
        public Double fat;
        public Double carbohydrates;
        public Integer quantity;
    }

    public static class AddItemRequest {
        public Long productId;
        public String name;
        public String barcode;
        public Double calories;
        public Double protein;
        public Double fat;
        public Double carbohydrates;
        public Integer quantity;
    }

    @GetMapping
    public ResponseEntity<List<ProductDto>> getItems(@PathVariable Long listId) {
        if (!listRepo.existsById(listId)) {
            return ResponseEntity.notFound().build();
        }
        List<ProductDto> result = itemRepo.findAllByShoppingListId(listId)
                .stream()
                .map(item -> {
                    Product p = item.getProduct();
                    ProductDto d = new ProductDto();
                    d.productId = p.getId();
                    d.itemId = item.getId(); // <-- ustawiamy itemId
                    d.name = p.getName();
                    d.barcode = p.getBarcode();
                    d.calories = p.getCalories();
                    d.protein = p.getProtein();
                    d.fat = p.getFat();
                    d.carbohydrates = p.getCarbohydrates();
                    d.quantity = item.getQuantity();
                    return d;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<?> addItem(@PathVariable Long listId, @RequestBody AddItemRequest req) {
        ShoppingList list = listRepo.findById(listId).orElse(null);
        if (list == null) {
            return ResponseEntity.badRequest().body("List not found");
        }

        Product product = null;
        if (req.productId != null) {
            product = productRepo.findById(req.productId).orElse(null);
            if (product == null) {
                return ResponseEntity.badRequest().body("Product id not found");
            }
        } else {
            if (req.barcode != null) {
                product = productRepo.findByBarcode(req.barcode).orElse(null);
            }
            if (product == null) {
                product = new Product(req.name, req.barcode, req.calories, req.protein, req.fat, req.carbohydrates);
                product = productRepo.save(product);
            }
        }

        int qty = req.quantity == null ? 1 : req.quantity;
        ShoppingListItem existing = itemRepo.findByShoppingListIdAndProductId(listId, product.getId());
        ShoppingListItem item;
        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + qty);
            item = itemRepo.save(existing);
        } else {
            item = new ShoppingListItem(list, product, qty);
            item = itemRepo.save(item);
        }

        ProductDto dto = new ProductDto();
        dto.productId = product.getId();
        dto.itemId = item.getId(); // <-- zwracamy itemId
        dto.name = product.getName();
        dto.barcode = product.getBarcode();
        dto.calories = product.getCalories();
        dto.protein = product.getProtein();
        dto.fat = product.getFat();
        dto.carbohydrates = product.getCarbohydrates();
        dto.quantity = item.getQuantity();

        return ResponseEntity.created(URI.create("/api/lists/" + listId + "/items/" + item.getId())).body(dto);
    }

    // update quantity for an existing item
    public static class UpdateQuantityRequest {
        public Integer quantity;
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<?> updateItemQuantity(@PathVariable Long listId, @PathVariable Long itemId, @RequestBody UpdateQuantityRequest req) {
        if (req == null || req.quantity == null || req.quantity < 0) {
            return ResponseEntity.badRequest().body("quantity required and must be >= 0");
        }
        ShoppingListItem item = itemRepo.findById(itemId).orElse(null);
        if (item == null || !item.getShoppingList().getId().equals(listId)) {
            return ResponseEntity.notFound().build();
        }
        if (req.quantity == 0) {
            itemRepo.delete(item);
            return ResponseEntity.noContent().build();
        }
        item.setQuantity(req.quantity);
        ShoppingListItem saved = itemRepo.save(item);

        Product p = saved.getProduct();
        ProductDto dto = new ProductDto();
        dto.productId = p.getId();
        dto.itemId = saved.getId(); // <-- itemId ustawiony
        dto.name = p.getName();
        dto.barcode = p.getBarcode();
        dto.calories = p.getCalories();
        dto.protein = p.getProtein();
        dto.fat = p.getFat();
        dto.carbohydrates = p.getCarbohydrates();
        dto.quantity = saved.getQuantity();

        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<?> deleteItem(@PathVariable Long listId, @PathVariable Long itemId) {
        ShoppingListItem item = itemRepo.findById(itemId).orElse(null);
        if (item == null || !item.getShoppingList().getId().equals(listId)) {
            return ResponseEntity.notFound().build();
        }
        itemRepo.delete(item);
        return ResponseEntity.noContent().build();
    }
}
