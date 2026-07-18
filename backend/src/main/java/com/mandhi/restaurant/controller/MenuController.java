package com.mandhi.restaurant.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class MenuController {

    @GetMapping(value = "/menu", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getMenu() {
        try {
            ClassPathResource resource = new ClassPathResource("menu.json");
            String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            return ResponseEntity.ok(content);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("[]");
        }
    }
}
