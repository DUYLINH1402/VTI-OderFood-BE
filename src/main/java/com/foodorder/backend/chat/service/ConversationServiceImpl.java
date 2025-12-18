package com.foodorder.backend.chat.service;

import com.foodorder.backend.chat.entity.Conversation;
import com.foodorder.backend.chat.repository.ConversationRepository;
import com.foodorder.backend.exception.BadRequestException;
import com.foodorder.backend.exception.ResourceNotFoundException;
import com.foodorder.backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service triển khai cho Conversation - quản lý cuộc trò chuyện duy nhất giữa User và Staff
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ConversationServiceImpl implements ConversationService {

    private final ConversationRepository conversationRepository;

    @Override
    public Conversation getOrCreateConversationForUser(User user) {
        log.info("Lấy hoặc tạo conversation cho user ID: {}", user.getId());

        // Kiểm tra xem user đã có conversation chưa
        return conversationRepository.findByUser(user)
                .orElseGet(() -> {
                    log.info("Tạo conversation mới cho user ID: {}", user.getId());
                    Conversation newConversation = Conversation.createForUser(user);
                    return conversationRepository.save(newConversation);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Conversation getConversationByUser(User user) {
        return conversationRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("CONVERSATION_NOT_FOUND",
                    "Không tìm thấy cuộc trò chuyện của user"));
    }

    @Override
    @Transactional(readOnly = true)
    public Conversation getConversationById(Long conversationId) {
        return conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("CONVERSATION_NOT_FOUND",
                    "Không tìm thấy cuộc trò chuyện"));
    }

    @Override
    public void updateLastMessageTime(Conversation conversation) {
        conversation.updateLastMessageTime();
        conversationRepository.save(conversation);
        log.debug("Cập nhật thời gian tin nhắn cuối cho conversation ID: {}", conversation.getId());
    }

    @Override
    public void addStaffNotes(Long conversationId, String notes, User staff) {
        if (notes == null || notes.trim().isEmpty()) {
            throw new BadRequestException("EMPTY_NOTES", "Ghi chú không được để trống");
        }

        Conversation conversation = getConversationById(conversationId);

        // Kiểm tra quyền staff
        String roleCode = staff.getRole().getCode();
        if (!"ROLE_STAFF".equals(roleCode) && !"ROLE_ADMIN".equals(roleCode)) {
            throw new BadRequestException("ACCESS_DENIED", "Chỉ staff mới có thể thêm ghi chú");
        }

        String noteWithStaff = String.format("[%s - %s]: %s",
            staff.getFullName(),
            LocalDateTime.now().toString(),
            notes.trim());

        conversation.addStaffNotes(noteWithStaff);
        conversationRepository.save(conversation);

        log.info("Staff {} đã thêm ghi chú cho conversation ID: {}", staff.getId(), conversationId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Conversation> getActiveConversations(Pageable pageable) {
        return conversationRepository.findActiveConversationsOrderByLastMessage(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Conversation> getAllConversations(Pageable pageable) {
        return conversationRepository.findAllConversationsOrderByLastMessage(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Conversation> searchConversations(String searchTerm, Pageable pageable) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getActiveConversations(pageable);
        }
        return conversationRepository.searchConversationsByUser(searchTerm.trim(), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Conversation> getRecentConversations(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return conversationRepository.findRecentActiveConversations(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countActiveConversations() {
        return conversationRepository.countActiveConversations();
    }

    @Override
    @Transactional(readOnly = true)
    public Long countConversationsWithNewMessagesSince(LocalDateTime since) {
        return conversationRepository.countConversationsWithNewMessagesSince(since);
    }

    @Override
    public void deactivateConversation(Long conversationId, User staff) {
        // Kiểm tra quyền staff
        String roleCode = staff.getRole().getCode();
        if (!"ROLE_STAFF".equals(roleCode) && !"ROLE_ADMIN".equals(roleCode)) {
            throw new BadRequestException("ACCESS_DENIED", "Chỉ staff mới có thể vô hiệu hóa cuộc trò chuyện");
        }

        Conversation conversation = getConversationById(conversationId);
        conversation.deactivate();

        // Thêm ghi chú về việc vô hiệu hóa
        String deactivateNote = String.format("Cuộc trò chuyện đã được vô hiệu hóa bởi %s", staff.getFullName());
        conversation.addStaffNotes(deactivateNote);

        conversationRepository.save(conversation);
        log.info("Staff {} đã vô hiệu hóa conversation ID: {}", staff.getId(), conversationId);
    }

    @Override
    public void activateConversation(Long conversationId, User staff) {
        // Kiểm tra quyền staff
        String roleCode = staff.getRole().getCode();
        if (!"ROLE_STAFF".equals(roleCode) && !"ROLE_ADMIN".equals(roleCode)) {
            throw new BadRequestException("ACCESS_DENIED", "Chỉ staff mới có thể kích hoạt cuộc trò chuyện");
        }

        Conversation conversation = getConversationById(conversationId);
        conversation.activate();

        // Thêm ghi chú về việc kích hoạt
        String activateNote = String.format("Cuộc trò chuyện đã được kích hoạt lại bởi %s", staff.getFullName());
        conversation.addStaffNotes(activateNote);

        conversationRepository.save(conversation);
        log.info("Staff {} đã kích hoạt lại conversation ID: {}", staff.getId(), conversationId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Conversation> getConversationsWithStaffNotes(Pageable pageable) {
        return conversationRepository.findConversationsWithStaffNotes(pageable);
    }

    @Override
    public void archiveInactiveConversations(int daysInactive) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysInactive);
        List<Conversation> inactiveConversations = conversationRepository.findInactiveConversationsBefore(cutoffDate);

        for (Conversation conversation : inactiveConversations) {
            conversation.deactivate();
            String archiveNote = String.format("Tự động archive do không hoạt động hơn %d ngày", daysInactive);
            conversation.addStaffNotes(archiveNote);
        }

        conversationRepository.saveAll(inactiveConversations);
        log.info("Đã archive {} conversation không hoạt động hơn {} ngày", inactiveConversations.size(), daysInactive);
    }
}
