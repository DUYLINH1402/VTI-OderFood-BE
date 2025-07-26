package com.foodorder.backend.feedbacks.controller;

import com.foodorder.backend.feedbacks.dto.reponse.FeedbackMediaResponse;
import com.foodorder.backend.feedbacks.dto.request.FeedbackMediaRequest;
import com.foodorder.backend.feedbacks.service.FeedbackMediaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/feedback-media")
public class FeedbackMediaController {

    @Autowired
    private FeedbackMediaService service;

    @GetMapping
    public List<FeedbackMediaResponse> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public FeedbackMediaResponse getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    public FeedbackMediaResponse create(@RequestBody FeedbackMediaRequest req) {
        return service.create(req);
    }

    @PutMapping("/{id}")
    public FeedbackMediaResponse update(@PathVariable Long id, @RequestBody FeedbackMediaRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}

