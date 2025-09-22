package com.foodorder.backend.points.service.impl;

import com.foodorder.backend.points.dto.response.PointsResponseDTO;
import com.foodorder.backend.points.dto.response.PointsHistoryDTO;
import com.foodorder.backend.points.entity.PointHistory;
import com.foodorder.backend.points.entity.RewardPoint;
import com.foodorder.backend.points.entity.PointType;
import com.foodorder.backend.points.repository.PointHistoryRepository;
import com.foodorder.backend.points.repository.RewardPointRepository;
import com.foodorder.backend.user.repository.UserRepository;
import com.foodorder.backend.exception.ResourceNotFoundException;
import com.foodorder.backend.points.service.PointsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PointsServiceImpl implements PointsService {

        private final RewardPointRepository rewardPointRepository;
        private final UserRepository userRepository;
        private final PointHistoryRepository pointHistoryRepository;

        // Cộng điểm và lưu log khi hoàn thành đơn hàng, khuyến mãi, v.v.
        @Override
        public void addPointsOnOrder(Long userId, Long orderId, int amount, String description) {
                if (amount <= 0) return;
                changePointsAndLog(userId, amount, orderId, description, PointType.EARN);
        }

        // Lấy điểm hiện tại của user theo username
        @Override
        public PointsResponseDTO getCurrentPointsByUsername(String username) {
                var user = userRepository.findByUsername(username)
                        .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND"));
                RewardPoint rewardPoint = rewardPointRepository.findByUser(user)
                        .orElse(RewardPoint.builder().user(user).balance(0).build());
                return new PointsResponseDTO(rewardPoint.getBalance());
        }

        //  CẢI THIỆN: Lịch sử điểm đơn giản và hiệu quả
        @Override
        public Page<PointsHistoryDTO> getPointsHistoryByUsername(String username, Pageable pageable) {
                var user = userRepository.findByUsername(username)
                        .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND"));

                // Tạo Pageable với sắp xếp mới nhất trước (DESC)
                Pageable sortedPageable = PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        Sort.by(Sort.Direction.DESC, "createdAt") // Mới nhất trước
                );

                // Query đơn giản, chỉ lấy dữ liệu cần thiết cho trang hiện tại
                Page<PointHistory> histories = pointHistoryRepository
                        .findByUserIdOrderByCreatedAtDesc(user.getId(), sortedPageable);

                // Map sang DTO đơn giản, không tính totalPointsAfter
                Page<PointsHistoryDTO> result = histories.map(history ->
                        PointsHistoryDTO.builder()
                                .id(history.getId())
                                .type(history.getType().name())
                                .amount(history.getAmount())
                                .orderId(history.getOrderId())
                                .description(history.getDescription())
                                .createdAt(history.getCreatedAt())
                                // Bỏ totalPointsAfter để tăng performance
                                .build()
                );

                return result;
        }

        // Trừ/cộng điểm và lưu log (tái sử dụng cho nhiều nghiệp vụ)
        private void changePointsAndLog(Long userId, int amount, Long orderId, String description, PointType type) {
                if (userId == null || amount == 0) return;

                var user = userRepository.findById(userId).orElse(null);
                if (user == null) return;

                RewardPoint rewardPoint = rewardPointRepository.findByUser(user)
                        .orElseGet(() -> {
                                RewardPoint newPoint = RewardPoint.builder()
                                        .user(user)
                                        .balance(0)
                                        .build();
                                return rewardPointRepository.save(newPoint);
                        });

                // Cập nhật balance
                int newBalance = rewardPoint.getBalance() + amount;
                rewardPoint.setBalance(Math.max(newBalance, 0)); // Không âm
                rewardPoint.setLastUpdated(LocalDateTime.now());
                rewardPointRepository.save(rewardPoint);

                // Lưu lịch sử
                PointHistory history = PointHistory.builder()
                        .userId(userId)
                        .type(type)
                        .amount(amount)
                        .orderId(orderId)
                        .description(description)
                        .createdAt(LocalDateTime.now())
                        .build();
                pointHistoryRepository.save(history);
        }

        // Trừ điểm và lưu log khi thanh toán đơn hàng
        @Override
        public void usePointsOnOrder(Long userId, Long orderId, int discountAmount, String description) {
                if (discountAmount <= 0) return;

                // KIỂM TRA: Đảm bảo user có đủ điểm trước khi trừ
                var user = userRepository.findById(userId).orElse(null);
                if (user == null) return;

                RewardPoint rewardPoint = rewardPointRepository.findByUser(user).orElse(null);
                if (rewardPoint == null || rewardPoint.getBalance() < discountAmount) {
                        throw new IllegalArgumentException("Không đủ điểm để sử dụng");
                }

                // Truyền số âm để trừ điểm
                changePointsAndLog(userId, -discountAmount, orderId, description, PointType.USE);
        }
}