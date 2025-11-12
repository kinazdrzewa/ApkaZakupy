package com.example.app.controller;

import com.example.app.model.ShoppingList;
import com.example.app.model.Product;
import com.example.app.repository.ShoppingListRepository;
import com.example.app.repository.ShoppingListItemRepository;
import com.example.app.repository.ProductRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/lists")
@CrossOrigin(origins = "*")
public class ShoppingListController {
    private final ShoppingListRepository repo;
    private final ShoppingListItemRepository itemRepo;
    private final ProductRepository productRepo;

    public ShoppingListController(ShoppingListRepository repo, ShoppingListItemRepository itemRepo, ProductRepository productRepo) {
        this.repo = repo;
        this.itemRepo = itemRepo;
        this.productRepo = productRepo;
    }

    @GetMapping
    public List<String> getAllNames(@RequestParam(required = false) Long userId) {
        if (userId == null) {
            return repo.findAll().stream().map(ShoppingList::getName).collect(Collectors.toList());
        } else {
            return repo.findAllByUserId(userId).stream().map(ShoppingList::getName).collect(Collectors.toList());
        }
    }

    @PostMapping
    public ResponseEntity<String> create(@RequestBody CreateRequest req) {
        if (req == null || req.name == null || req.name.isBlank() || req.userId == null) {
            return ResponseEntity.badRequest().body("userId and name required");
        }
        if (repo.findByNameAndUserId(req.name, req.userId).isPresent()) {
            return ResponseEntity.status(409).body("Already exists for this user");
        }
        ShoppingList s = new ShoppingList(req.userId, req.name);
        ShoppingList saved = repo.save(s);
        return ResponseEntity.created(URI.create("/api/lists/" + saved.getId())).body(saved.getName());
    }

    public static class CreateRequest {
        public String name;
        public Long userId;
    }

    // DTO for returning id + name
    public static class ListDto {
        public Long id;
        public String name;

        public ListDto(Long id, String name) { this.id = id; this.name = name; }
    }

    @GetMapping("/details")
    public List<ListDto> getAllDetails(@RequestParam(required = false) Long userId) {
        if (userId == null) {
            return repo.findAll().stream().map(s -> new ListDto(s.getId(), s.getName())).collect(Collectors.toList());
        } else {
            return repo.findAllByUserId(userId).stream().map(s -> new ListDto(s.getId(), s.getName())).collect(Collectors.toList());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteList(@PathVariable Long id) {
        if (!repo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        // delete all items belonging to this list first
        itemRepo.findAllByShoppingListId(id).forEach(itemRepo::delete);
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Item endpoints moved to ShoppingListItemController to avoid duplicate mappings
}
