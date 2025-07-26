package com.foodorder.backend.feedbacks.repository;

import com.foodorder.backend.feedbacks.entity.FeedbackMedia;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackMediaRepository extends JpaRepository<FeedbackMedia, Long> {
    // Thừa hưởng sẵn CRUD
}

