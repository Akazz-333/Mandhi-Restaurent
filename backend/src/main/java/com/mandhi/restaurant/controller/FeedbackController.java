package com.mandhi.restaurant.controller;

import com.mandhi.restaurant.entity.Feedback;
import com.mandhi.restaurant.repository.FeedbackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class FeedbackController {

    @Autowired
    private FeedbackRepository feedbackRepository;

    @PostMapping("/feedbacks")
    public ResponseEntity<Feedback> createFeedback(@RequestBody Feedback feedback) {
        if (feedback.getRating() == null) {
            return ResponseEntity.badRequest().build();
        }
        Feedback savedFeedback = feedbackRepository.save(feedback);
        return ResponseEntity.ok(savedFeedback);
    }

    @GetMapping("/feedbacks")
    public ResponseEntity<List<Feedback>> getAllFeedbacks() {
        List<Feedback> feedbacks = feedbackRepository.findAll();
        return ResponseEntity.ok(feedbacks);
    }

    @DeleteMapping("/feedbacks/{id}")
    public ResponseEntity<Void> deleteFeedback(@PathVariable Long id) {
        if (!feedbackRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        feedbackRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
