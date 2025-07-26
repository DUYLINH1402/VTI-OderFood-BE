package com.foodorder.backend.feedbacks.dto.reponse;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;


@Getter
@Setter
public class FeedbackMediaResponse {
    private Long id;
    private String type;
    private String mediaUrl;
    private String thumbnailUrl;
    private Integer displayOrder;
    private Timestamp createdAt;
}
