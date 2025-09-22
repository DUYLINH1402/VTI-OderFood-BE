package com.foodorder.backend.chatbot.service;

import com.foodorder.backend.chatbot.dto.KnowledgeBaseDTO;
import com.foodorder.backend.chatbot.entity.KnowledgeBase;
import com.foodorder.backend.chatbot.repository.KnowledgeBaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service quản lý Knowledge Base cho hệ thống RAG
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KnowledgeBaseService {

    private final KnowledgeBaseRepository knowledgeBaseRepository;
    private final ModelMapper modelMapper;

    /**
     * Tạo mới knowledge base
     */
    @Transactional
    public KnowledgeBaseDTO createKnowledgeBase(KnowledgeBaseDTO dto, Long createdBy) {
        try {
            KnowledgeBase entity = modelMapper.map(dto, KnowledgeBase.class);
            entity.setId(null); // Đảm bảo tạo mới
            entity.setCreatedBy(createdBy);
            entity.setCreatedAt(LocalDateTime.now());

            if (entity.getPriority() == null) {
                entity.setPriority(1);
            }

            if (entity.getIsActive() == null) {
                entity.setIsActive(true);
            }

            KnowledgeBase saved = knowledgeBaseRepository.save(entity);
//            log.info("Đã tạo knowledge base mới với ID: {}", saved.getId());

            return modelMapper.map(saved, KnowledgeBaseDTO.class);

        } catch (Exception e) {
            log.error("Lỗi khi tạo knowledge base: {}", e.getMessage());
            throw new RuntimeException("Không thể tạo knowledge base: " + e.getMessage());
        }
    }

    /**
     * Cập nhật knowledge base
     */
    @Transactional
    public KnowledgeBaseDTO updateKnowledgeBase(Long id, KnowledgeBaseDTO dto, Long updatedBy) {
        try {
            Optional<KnowledgeBase> existingOpt = knowledgeBaseRepository.findById(id);
            if (existingOpt.isEmpty()) {
                throw new RuntimeException("Không tìm thấy knowledge base với ID: " + id);
            }

            KnowledgeBase existing = existingOpt.get();

            // Cập nhật các trường
            existing.setTitle(dto.getTitle());
            existing.setContent(dto.getContent());
            existing.setKeywords(dto.getKeywords());
            existing.setCategory(dto.getCategory());
            existing.setPriority(dto.getPriority());
            existing.setIsActive(dto.getIsActive());
            existing.setUpdatedAt(LocalDateTime.now());

            KnowledgeBase updated = knowledgeBaseRepository.save(existing);
//            log.info("Đã cập nhật knowledge base ID: {}", updated.getId());

            return modelMapper.map(updated, KnowledgeBaseDTO.class);

        } catch (Exception e) {
            log.error("Lỗi khi cập nhật knowledge base: {}", e.getMessage());
            throw new RuntimeException("Không thể cập nhật knowledge base: " + e.getMessage());
        }
    }

    /**
     * Lấy knowledge base theo ID
     */
    public KnowledgeBaseDTO getKnowledgeBaseById(Long id) {
        Optional<KnowledgeBase> entity = knowledgeBaseRepository.findById(id);
        if (entity.isEmpty()) {
            throw new RuntimeException("Không tìm thấy knowledge base với ID: " + id);
        }

        return modelMapper.map(entity.get(), KnowledgeBaseDTO.class);
    }

    /**
     * Lấy danh sách knowledge base với phân trang
     */
    public Page<KnowledgeBaseDTO> getKnowledgeBasePage(Pageable pageable) {
        Page<KnowledgeBase> entities = knowledgeBaseRepository
            .findByIsActiveTrueOrderByPriorityDescCreatedAtDesc(pageable);

        return entities.map(entity -> modelMapper.map(entity, KnowledgeBaseDTO.class));
    }

    /**
     * Tìm kiếm knowledge base theo từ khóa
     */
    public List<KnowledgeBaseDTO> searchKnowledgeBase(String keyword) {
        List<KnowledgeBase> entities = knowledgeBaseRepository.searchByKeyword(keyword);

        return entities.stream()
            .map(entity -> modelMapper.map(entity, KnowledgeBaseDTO.class))
            .collect(Collectors.toList());
    }

    /**
     * Lấy knowledge base theo danh mục
     */
    public List<KnowledgeBaseDTO> getKnowledgeBaseByCategory(KnowledgeBase.KnowledgeCategory category) {
        List<KnowledgeBase> entities = knowledgeBaseRepository
            .findByCategoryAndIsActiveTrueOrderByPriorityDescCreatedAtDesc(category);

        return entities.stream()
            .map(entity -> modelMapper.map(entity, KnowledgeBaseDTO.class))
            .collect(Collectors.toList());
    }

    /**
     * Xóa knowledge base (soft delete)
     */
    @Transactional
    public boolean deleteKnowledgeBase(Long id) {
        try {
            Optional<KnowledgeBase> entityOpt = knowledgeBaseRepository.findById(id);
            if (entityOpt.isEmpty()) {
                return false;
            }

            KnowledgeBase entity = entityOpt.get();
            entity.setIsActive(false);
            entity.setUpdatedAt(LocalDateTime.now());

            knowledgeBaseRepository.save(entity);
//            log.info("Đã vô hiệu hóa knowledge base ID: {}", id);

            return true;

        } catch (Exception e) {
            log.error("Lỗi khi xóa knowledge base: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Khởi tạo dữ liệu mẫu cho knowledge base
     */
    @Transactional
    public void initializeSampleData() {
        try {
            // Kiểm tra xem đã có dữ liệu chưa
            long count = knowledgeBaseRepository.count();
            if (count > 0) {
//                log.info("Knowledge base đã có dữ liệu, bỏ qua khởi tạo mẫu");
                return;
            }

//            log.info("Khởi tạo dữ liệu mẫu cho knowledge base");

            // Thông tin nhà hàng
            createSampleKnowledge(
                "Giới thiệu nhà hàng",
                "Chúng tôi là hệ thống nhà hàng trực tuyến chuyên phục vụ các món ăn ngon, chất lượng cao. " +
                "Với đội ngũ đầu bếp giàu kinh nghiệm và nguyên liệu tươi ngon, chúng tôi cam kết mang đến " +
                "những trải nghiệm ẩm thực tuyệt vời cho khách hàng.",
                "nhà hàng, giới thiệu, về chúng tôi, food order",
                KnowledgeBase.KnowledgeCategory.RESTAURANT_INFO,
                10
            );

            // Giờ hoạt động
            createSampleKnowledge(
                "Giờ hoạt động của nhà hàng",
                "Nhà hàng hoạt động từ 8:00 đến 22:00 hàng ngày. " +
                "Dịch vụ giao hàng: 9:00 - 21:30. " +
                "Chúng tôi phục vụ 7 ngày trong tuần, kể cả cuối tuần và ngày lễ.",
                "giờ hoạt động, mở cửa, đóng cửa, time, hours",
                KnowledgeBase.KnowledgeCategory.OPERATING_HOURS,
                9
            );

            // Chính sách đặt hàng
            createSampleKnowledge(
                "Cách thức đặt hàng",
                "Khách hàng có thể đặt hàng qua website hoặc điện thoại trực tiếp " +
                "Quy trình: Chọn món → Thêm vào giỏ hàng → Thanh toán → Xác nhận đơn hàng. " +
                "Đơn hàng tối thiểu: 50,000 VNĐ cho giao hàng miễn phí trong nội thành.",
                "đặt hàng, order, cách đặt, quy trình",
                KnowledgeBase.KnowledgeCategory.ORDER_POLICY,
                8
            );

            // Thông tin thanh toán
            createSampleKnowledge(
                "Phương thức thanh toán",
                "Chúng tôi hỗ trợ các phương thức thanh toán: " +
                "- Tiền mặt khi nhận hàng (COD) " +
                "- Chuyển khoản ngân hàng " +
                "- Ví điện tử: ZaloPay, MoMo, VNPay " +
                "- Thẻ tín dụng/ghi nợ",
                "thanh toán, payment, tiền, pay, cod, zalopay, momo",
                KnowledgeBase.KnowledgeCategory.PAYMENT_INFO,
                8
            );

            // Thông tin giao hàng
            createSampleKnowledge(
                "Chính sách giao hàng",
                "Phí giao hàng: tuỳ theo khu vực (miễn phí cho đơn từ 500,000 VNĐ). " +
                "Thời gian giao hàng: 30-45 phút trong nội thành, 45-60 phút ngoại thành. " +
                "Khu vực giao hàng: Toàn thành phố và các quận lân cận trong bán kính 15km.",
                "giao hàng, delivery, ship, phí giao hàng, thời gian giao",
                KnowledgeBase.KnowledgeCategory.DELIVERY_INFO,
                8
            );

            // Liên hệ
            createSampleKnowledge(
                "Thông tin liên hệ",
                "Hotline: 1900-0000 (8:00 - 22:00 hàng ngày) " +
                "Email: support@dongxanhfood.com " +
                "Facebook: facebook.com/donhxanhfood " +
                "Địa chỉ: 211 Nguyễn Văn Linh, P. Hưng Lợi, Q. Ninh Kiều, TP. Cần Thơ",
                "liên hệ, contact, hotline, email, facebook, địa chỉ",
                KnowledgeBase.KnowledgeCategory.CONTACT,
                9
            );

            // FAQ
            createSampleKnowledge(
                "Câu hỏi thường gặp",
                "Q: Làm sao để theo dõi đơn hàng? " +
                "A: Sau khi đặt hàng thành công, bạn sẽ nhận được mã đơn hàng qua SMS/Email để theo dõi. " +
                "Q: Có thể hủy đơn hàng không? " +
                "A: Có thể hủy trong vòng 5 phút sau khi đặt, sau đó vui lòng liên hệ hotline. " +
                "Q: Có món chay không? " +
                "A: Có, chúng tôi có menu chay đa dạng.",
                "faq, hỏi đáp, hủy đơn, theo dõi, món chay",
                KnowledgeBase.KnowledgeCategory.FAQ,
                7
            );

//            log.info("Đã khởi tạo xong dữ liệu mẫu cho knowledge base");

        } catch (Exception e) {
            log.error("Lỗi khi khởi tạo dữ liệu mẫu: {}", e.getMessage());
        }
    }

    private void createSampleKnowledge(String title, String content, String keywords,
                                     KnowledgeBase.KnowledgeCategory category, int priority) {
        KnowledgeBase knowledge = KnowledgeBase.builder()
                .title(title)
                .content(content)
                .keywords(keywords)
                .category(category)
                .priority(priority)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .createdBy(1L) // Admin user
                .build();

        knowledgeBaseRepository.save(knowledge);
    }
}
