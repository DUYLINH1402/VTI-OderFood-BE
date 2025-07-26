package com.foodorder.backend.feedbacks.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FeedbackMediaRequest {
    private String type; // "IMAGE" hoặc "VIDEO"
    private String mediaUrl;
    private String thumbnailUrl;
    private Integer displayOrder;
}
