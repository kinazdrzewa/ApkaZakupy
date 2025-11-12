package com.example.app.controller;

import com.example.app.model.Suggestion;
import com.example.app.repository.SuggestionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/suggestions")
@CrossOrigin(origins = "*")
public class SuggestionController {

    private final SuggestionRepository suggestionRepository;

    public SuggestionController(SuggestionRepository suggestionRepository) {
        this.suggestionRepository = suggestionRepository;
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
}
