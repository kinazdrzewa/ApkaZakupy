package com.example.app.controller;

import com.example.app.model.Suggestion;
import com.example.app.repository.SuggestionRepository;
import com.example.app.repository.ProductRepository;
import com.example.app.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/suggestions")
@CrossOrigin(origins = "*")
public class SuggestionController {

    private final SuggestionRepository suggestionRepository;
    private final ProductRepository productRepository;

    private static final Logger logger = LoggerFactory.getLogger(SuggestionController.class);

    public SuggestionController(SuggestionRepository suggestionRepository, ProductRepository productRepository) {
        this.suggestionRepository = suggestionRepository;
        this.productRepository = productRepository;
    }

    @GetMapping
    public List<Suggestion> getAll(@RequestParam(required = false) Long userId) {
        if (userId != null) return suggestionRepository.findAllByUserId(userId);
        return suggestionRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<Suggestion> create(@RequestBody Suggestion suggestion) {
        Suggestion saved = suggestionRepository.save(suggestion);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!suggestionRepository.existsById(id)) return ResponseEntity.notFound().build();
        suggestionRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/accept")
    @Transactional
    public ResponseEntity<Product> acceptSuggestion(@PathVariable Long id) {
        logger.info("Accepting suggestion id={}", id);
        var opt = suggestionRepository.findById(id);
        if (opt.isEmpty()) {
            logger.warn("Suggestion id={} not found", id);
            return ResponseEntity.notFound().build();
        }
        Suggestion s = opt.get();
        try {
            Product p = new Product(s.getProductName(), s.getBarcode(), s.getCalories(), s.getProtein(), s.getFat(), s.getCarbohydrates());
            Product saved = productRepository.save(p);
            suggestionRepository.deleteById(id);
            logger.info("Created product id={} from suggestion id={}", saved.getId(), id);
            return ResponseEntity.ok(saved);
        } catch (Exception ex) {
            logger.error("Error accepting suggestion id={}: {}", id, ex.getMessage(), ex);
            return ResponseEntity.status(500).build();
        }
    }
}
