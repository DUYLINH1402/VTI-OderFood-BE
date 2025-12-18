package com.foodorder.backend.chatbot.repository;

import com.foodorder.backend.chatbot.entity.KnowledgeBase;
import com.foodorder.backend.chatbot.entity.KnowledgeBase.KnowledgeCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository để quản lý Knowledge Base cho hệ thống RAG
 */
@Repository
public interface KnowledgeBaseRepository extends JpaRepository<KnowledgeBase, Long> {

    /**
     * Tìm kiếm knowledge base theo từ khóa (full-text search)
     */
    @Query("SELECT kb FROM KnowledgeBase kb WHERE kb.isActive = true AND " +
           "(LOWER(kb.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(kb.content) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(kb.keywords) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY kb.priority DESC, kb.createdAt DESC")
    List<KnowledgeBase> searchByKeyword(@Param("keyword") String keyword);

    /**
     * Tìm kiếm với nhiều từ khóa
     */
    @Query("SELECT DISTINCT kb FROM KnowledgeBase kb WHERE kb.isActive = true AND " +
           "(LOWER(kb.title) LIKE LOWER(CONCAT('%', :keyword1, '%')) OR " +
           "LOWER(kb.content) LIKE LOWER(CONCAT('%', :keyword1, '%')) OR " +
           "LOWER(kb.keywords) LIKE LOWER(CONCAT('%', :keyword1, '%')) OR " +
           "LOWER(kb.title) LIKE LOWER(CONCAT('%', :keyword2, '%')) OR " +
           "LOWER(kb.content) LIKE LOWER(CONCAT('%', :keyword2, '%')) OR " +
           "LOWER(kb.keywords) LIKE LOWER(CONCAT('%', :keyword2, '%'))) " +
           "ORDER BY kb.priority DESC, kb.createdAt DESC")
    List<KnowledgeBase> searchByMultipleKeywords(@Param("keyword1") String keyword1,
                                                @Param("keyword2") String keyword2);

    /**
     * Lấy knowledge base theo danh mục
     */
    List<KnowledgeBase> findByCategoryAndIsActiveTrueOrderByPriorityDescCreatedAtDesc(
            KnowledgeCategory category);

    /**
     * Lấy knowledge base có độ ưu tiên cao
     */
    @Query("SELECT kb FROM KnowledgeBase kb WHERE kb.isActive = true AND kb.priority >= :minPriority " +
           "ORDER BY kb.priority DESC, kb.createdAt DESC")
    List<KnowledgeBase> findHighPriorityKnowledge(@Param("minPriority") Integer minPriority);

    /**
     * Tìm kiếm knowledge base theo danh mục và từ khóa
     */
    @Query("SELECT kb FROM KnowledgeBase kb WHERE kb.isActive = true AND kb.category = :category AND " +
           "(LOWER(kb.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(kb.content) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(kb.keywords) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY kb.priority DESC, kb.createdAt DESC")
    List<KnowledgeBase> findByCategoryAndKeyword(@Param("category") KnowledgeCategory category,
                                               @Param("keyword") String keyword);

    /**
     * Lấy tất cả knowledge base đang hoạt động
     */
    List<KnowledgeBase> findByIsActiveTrueOrderByPriorityDescCreatedAtDesc();

    /**
     * Phân trang knowledge base
     */
    Page<KnowledgeBase> findByIsActiveTrueOrderByPriorityDescCreatedAtDesc(Pageable pageable);

    /**
     * Đếm số lượng knowledge base theo danh mục
     */
    Long countByCategoryAndIsActiveTrue(KnowledgeCategory category);
}

