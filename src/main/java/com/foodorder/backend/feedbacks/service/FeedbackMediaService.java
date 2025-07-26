package com.foodorder.backend.feedbacks.service;

import com.foodorder.backend.feedbacks.dto.reponse.FeedbackMediaResponse;
import com.foodorder.backend.feedbacks.dto.request.FeedbackMediaRequest;

import java.util.List;

public interface FeedbackMediaService {
    List<FeedbackMediaResponse> getAll();
    FeedbackMediaResponse getById(Long id);
    FeedbackMediaResponse create(FeedbackMediaRequest request);
    FeedbackMediaResponse update(Long id, FeedbackMediaRequest request);
    void delete(Long id);
}
