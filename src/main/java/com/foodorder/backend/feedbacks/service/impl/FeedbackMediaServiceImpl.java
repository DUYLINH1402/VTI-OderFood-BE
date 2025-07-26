package com.foodorder.backend.feedbacks.service.impl;

import com.foodorder.backend.exception.ResourceNotFoundException;
import com.foodorder.backend.feedbacks.dto.reponse.FeedbackMediaResponse;
import com.foodorder.backend.feedbacks.dto.request.FeedbackMediaRequest;
import com.foodorder.backend.feedbacks.entity.FeedbackMedia;
import com.foodorder.backend.feedbacks.repository.FeedbackMediaRepository;
import com.foodorder.backend.feedbacks.service.FeedbackMediaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class FeedbackMediaServiceImpl implements FeedbackMediaService {

    @Autowired
    private FeedbackMediaRepository repo;

    @Override
    public List<FeedbackMediaResponse> getAll() {
        return repo.findAll(Sort.by("displayOrder"))
                .stream().map(this::toResponse).toList();
    }

    @Override
    public FeedbackMediaResponse getById(Long id) {
        FeedbackMedia media = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback media not found", "NOT_FOUND"));
        return toResponse(media);
    }

    @Override
    public FeedbackMediaResponse create(FeedbackMediaRequest req) {
        FeedbackMedia media = new FeedbackMedia();
        media.setType(FeedbackMedia.MediaType.valueOf(req.getType())); // nhớ validate đầu vào
        media.setMediaUrl(req.getMediaUrl());
        media.setThumbnailUrl(req.getThumbnailUrl());
        media.setDisplayOrder(req.getDisplayOrder() != null ? req.getDisplayOrder() : 0);
        repo.save(media);
        return toResponse(media);
    }

    @Override
    public FeedbackMediaResponse update(Long id, FeedbackMediaRequest req) {
        FeedbackMedia media = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback media not found", "NOT_FOUND"));
        media.setType(FeedbackMedia.MediaType.valueOf(req.getType()));
        media.setMediaUrl(req.getMediaUrl());
        media.setThumbnailUrl(req.getThumbnailUrl());
        media.setDisplayOrder(req.getDisplayOrder() != null ? req.getDisplayOrder() : 0);
        repo.save(media);
        return toResponse(media);
    }

    @Override
    public void delete(Long id) {
        repo.deleteById(id);
    }

    private FeedbackMediaResponse toResponse(FeedbackMedia media) {
        FeedbackMediaResponse resp = new FeedbackMediaResponse();
        resp.setId(media.getId());
        resp.setType(media.getType().name());
        resp.setMediaUrl(media.getMediaUrl());
        resp.setThumbnailUrl(media.getThumbnailUrl());
        resp.setDisplayOrder(media.getDisplayOrder());
        resp.setCreatedAt(media.getCreatedAt());
        return resp;
    }
}


