package com.foodorder.backend.coupons.service.impl;

import com.foodorder.backend.config.CacheConfig;
import com.foodorder.backend.coupons.dto.request.ApplyCouponRequest;
import com.foodorder.backend.coupons.dto.request.CouponRequest;
import com.foodorder.backend.coupons.dto.response.CouponApplyResult;
import com.foodorder.backend.coupons.dto.response.CouponResponse;
import com.foodorder.backend.coupons.entity.*;
import com.foodorder.backend.coupons.repository.CouponRepository;
import com.foodorder.backend.coupons.repository.CouponUsageRepository;
import com.foodorder.backend.category.repository.CategoryRepository;
import com.foodorder.backend.coupons.service.CouponService;
import com.foodorder.backend.food.repository.FoodRepository;
import com.foodorder.backend.user.repository.UserRepository;
import com.foodorder.backend.user.entity.User;
import com.foodorder.backend.order.entity.Order;
import com.foodorder.backend.order.repository.OrderRepository;
import com.foodorder.backend.exception.BadRequestException;
import com.foodorder.backend.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Triển khai nghiệp vụ cho module Coupon
 * Tuân thủ kiến trúc Spring Boot: tách interface và lớp triển khai, comment rõ ràng cho từng nghiệp vụ
 */
@Service
@Transactional
public class CouponServiceImpl implements CouponService {

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private CouponUsageRepository couponUsageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private FoodRepository foodRepository;

    @Autowired
    private OrderRepository orderRepository;

    // === QUẢN LÝ COUPON CƠ BẢN ===

    @Override
    @CacheEvict(value = CacheConfig.ACTIVE_COUPONS_CACHE, allEntries = true)
    public CouponResponse createCoupon(CouponRequest request) {
        // Validate business rules
        validateCouponRequest(request);

        // Check code uniqueness
        if (couponRepository.findByCode(request.getCode()).isPresent()) {
            throw new BadRequestException("Coupon code already exists: " + request.getCode(), "COUPON_CODE_EXISTS");
        }

        // Build coupon entity
        Coupon coupon = Coupon.builder()
                .code(request.getCode().toUpperCase())
                .title(request.getTitle())
                .description(request.getDescription())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .minOrderAmount(request.getMinOrderAmount())
                .maxDiscountAmount(request.getMaxDiscountAmount())
                .maxUsagePerUser(request.getMaxUsagePerUser())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .maxUsage(request.getMaxUsage())
                .usedCount(0)
                .status(CouponStatus.ACTIVE)
                .couponType(request.getCouponType())
                .build();

        // Set relationships
        setApplicableEntities(coupon, request);

        // Save to database
        coupon = couponRepository.save(coupon);

        return CouponResponse.fromEntity(coupon);
    }

    @Override
    @CacheEvict(value = CacheConfig.ACTIVE_COUPONS_CACHE, allEntries = true)
    public CouponResponse updateCoupon(Long id, CouponRequest request) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found with id: " + id, "COUPON_NOT_FOUND"));

        // Validate business rules
        validateCouponRequest(request);

        // Check code uniqueness (exclude current coupon)
        Optional<Coupon> existingCoupon = couponRepository.findByCode(request.getCode());
        if (existingCoupon.isPresent() && !existingCoupon.get().getId().equals(id)) {
            throw new BadRequestException("Coupon code already exists: " + request.getCode(), "COUPON_CODE_EXISTS");
        }

        // Update fields
        coupon.setCode(request.getCode().toUpperCase());
        coupon.setTitle(request.getTitle());
        coupon.setDescription(request.getDescription());
        coupon.setDiscountType(request.getDiscountType());
        coupon.setDiscountValue(request.getDiscountValue());
        coupon.setMinOrderAmount(request.getMinOrderAmount());
        coupon.setMaxDiscountAmount(request.getMaxDiscountAmount());
        coupon.setMaxUsagePerUser(request.getMaxUsagePerUser());
        coupon.setStartDate(request.getStartDate());
        coupon.setEndDate(request.getEndDate());
        coupon.setMaxUsage(request.getMaxUsage());
        coupon.setCouponType(request.getCouponType());

        // Update relationships
        setApplicableEntities(coupon, request);

        coupon = couponRepository.save(coupon);
        return CouponResponse.fromEntity(coupon);
    }

    @Override
    @CacheEvict(value = CacheConfig.ACTIVE_COUPONS_CACHE, allEntries = true)
    public void deleteCoupon(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found with id: " + id, "COUPON_NOT_FOUND"));

        // Soft delete - change status to INACTIVE
        coupon.setStatus(CouponStatus.INACTIVE);
        couponRepository.save(coupon);
    }

    @Override
    public Optional<CouponResponse> getCouponById(Long id) {
        return couponRepository.findById(id)
                .map(CouponResponse::fromEntity);
    }

    @Override
    public Optional<CouponResponse> getCouponByCode(String code) {
        return couponRepository.findByCode(code.toUpperCase())
                .map(CouponResponse::fromEntity);
    }

    @Override
    public List<CouponResponse> getCouponsByStatus(CouponStatus status) {
        return couponRepository.findByStatus(status).stream()
                .map(CouponResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Page<CouponResponse> getAllCoupons(Pageable pageable) {
        return couponRepository.findAll(pageable)
                .map(CouponResponse::fromEntity);
    }

    // === LOGIC NGHIỆP VỤ COUPON ===

    @Override
    @Cacheable(value = CacheConfig.ACTIVE_COUPONS_CACHE, key = "'public'")
    public List<CouponResponse> getActivePublicCoupons() {
        return couponRepository.findActivePublicCoupons(LocalDateTime.now()).stream()
                .map(CouponResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<CouponResponse> getAvailableCouponsForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId, "USER_NOT_FOUND"));

        return couponRepository.findAvailableCouponsForUser(user, LocalDateTime.now()).stream()
                .map(CouponResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public CouponApplyResult validateCouponForOrder(ApplyCouponRequest request) {
        try {
            // Find coupon
            Coupon coupon = couponRepository.findByCode(request.getCouponCode().toUpperCase())
                    .orElse(null);

            if (coupon == null) {
                return CouponApplyResult.failure("COUPON_NOT_FOUND");
            }

            // Find user
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found", "USER_NOT_FOUND"));

            // Get user usage count for this coupon
            long userUsageCount = couponUsageRepository.countByCouponAndUser(coupon, user);

            // Validate coupon conditions
            if (!coupon.canUserUseCoupon(user, userUsageCount)) {
                return CouponApplyResult.failure("COUPON_NOT_VALID");
            }

            // Check minimum order amount
            if (coupon.getMinOrderAmount() != null && request.getOrderAmount() < coupon.getMinOrderAmount()) {
                return CouponApplyResult.failure(
                    String.format("MIN_ORDER_AMOUNT_NOT_MET", coupon.getMinOrderAmount())

                );
            }

            // Check applicable foods (if specified)
            if (coupon.getApplicableFoods() != null && !coupon.getApplicableFoods().isEmpty()) {
                // Kiểm tra request có foodIds không
                if (request.getFoodIds() == null || request.getFoodIds().isEmpty()) {
                    return CouponApplyResult.failure("NO_FOOD_ITEMS");
                }

                boolean hasApplicableFood = request.getFoodIds().stream()
                    .anyMatch(foodId -> coupon.getApplicableFoods().stream()
                        .anyMatch(food -> food.getId().equals(foodId)));

                if (!hasApplicableFood) {
                    return CouponApplyResult.failure("COUPON_NOT_APPLICABLE");
                }
            }

            // Calculate discount
            double discountAmount = coupon.calculateDiscountAmount(request.getOrderAmount());

            return CouponApplyResult.success(
                coupon.getCode(),
                coupon.getTitle(),
                request.getOrderAmount(),
                discountAmount
            );

        } catch (Exception e) {
            return CouponApplyResult.failure("COUPON_VALIDATION_ERROR" + e.getMessage());
        }
    }

    @Override
    public CouponApplyResult applyCouponToOrder(ApplyCouponRequest request) {
        // Validate first
        CouponApplyResult validationResult = validateCouponForOrder(request);
        if (!validationResult.getSuccess()) {
            return validationResult;
        }

        // Return successful result with discount calculation
        return validationResult;
    }

    @Override
    public void confirmCouponUsage(String couponCode, Long userId, Long orderId, Double discountAmount) {
        Coupon coupon = couponRepository.findByCode(couponCode.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found: " + couponCode, "COUPON_NOT_FOUND"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId, "USER_NOT_FOUND"));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId, "ORDER_NOT_FOUND"));

        // Create usage record
        CouponUsage usage = CouponUsage.builder()
                .coupon(coupon)
                .user(user)
                .order(order)
                .discountAmount(discountAmount)
                .usedAt(LocalDateTime.now())
                .build();

        couponUsageRepository.save(usage);

        // Update coupon used count
        coupon.setUsedCount(coupon.getUsedCount() + 1);

        // Check if coupon is used out
        if (coupon.getUsedCount() >= coupon.getMaxUsage()) {
            coupon.setStatus(CouponStatus.USED_OUT);
        }

        couponRepository.save(coupon);
    }

    @Override
    public void cancelCouponUsage(Long couponUsageId) {
        CouponUsage usage = couponUsageRepository.findById(couponUsageId)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon usage not found with id: " + couponUsageId, "COUPON_USAGE_NOT_FOUND"));

        Coupon coupon = usage.getCoupon();

        // Delete usage record
        couponUsageRepository.delete(usage);

        // Decrease used count
        coupon.setUsedCount(Math.max(0, coupon.getUsedCount() - 1));

        // Reactivate if was used out
        if (coupon.getStatus() == CouponStatus.USED_OUT && coupon.getUsedCount() < coupon.getMaxUsage()) {
            coupon.setStatus(CouponStatus.ACTIVE);
        }

        couponRepository.save(coupon);
    }

    // === QUẢN LÝ TRẠNG THÁI COUPON ===

    @Override
    public void activateCoupon(Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found with id: " + couponId, "COUPON_NOT_FOUND"));

        coupon.setStatus(CouponStatus.ACTIVE);
        couponRepository.save(coupon);
    }

    @Override
    public void deactivateCoupon(Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found with id: " + couponId, "COUPON_NOT_FOUND"));

        coupon.setStatus(CouponStatus.INACTIVE);
        couponRepository.save(coupon);
    }

    @Override
    public void updateExpiredCoupons() {
        LocalDateTime now = LocalDateTime.now();
        List<Coupon> expiredCoupons = couponRepository.findAll().stream()
                .filter(coupon -> coupon.getStatus() == CouponStatus.ACTIVE && coupon.getEndDate().isBefore(now))
                .collect(Collectors.toList());

        expiredCoupons.forEach(coupon -> coupon.setStatus(CouponStatus.EXPIRED));
        couponRepository.saveAll(expiredCoupons);
    }

    @Override
    public void updateUsedOutCoupons() {
        List<Coupon> usedOutCoupons = couponRepository.findUsedOutCoupons().stream()
                .filter(coupon -> coupon.getStatus() == CouponStatus.ACTIVE)
                .collect(Collectors.toList());

        usedOutCoupons.forEach(coupon -> coupon.setStatus(CouponStatus.USED_OUT));
        couponRepository.saveAll(usedOutCoupons);
    }

    // === BÁO CÁO VÀ THỐNG KÊ ===

    @Override
    public Map<CouponStatus, Long> getCouponStatistics() {
        Map<CouponStatus, Long> stats = new HashMap<>();
        for (CouponStatus status : CouponStatus.values()) {
            stats.put(status, couponRepository.countByStatus(status));
        }
        return stats;
    }

    @Override
    public List<CouponResponse> getMostUsedCoupons(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by("usedCount").descending());
        return couponRepository.findAll(pageable).stream()
                .map(CouponResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<CouponResponse> getExpiringSoonCoupons(int daysAhead) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime futureDate = now.plusDays(daysAhead);

        return couponRepository.findCouponsExpiringBetween(now, futureDate).stream()
                .map(CouponResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // === HỖ TRỢ BUSINESS ===

    @Override
    public void createBirthdayCouponForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId, "USER_NOT_FOUND"));

        String couponCode = "BIRTHDAY" + userId + "_" + LocalDateTime.now().getYear();

        // Check if birthday coupon already exists for this year
        if (couponRepository.findByCode(couponCode).isPresent()) {
            return; // Already created
        }

        Coupon birthdayCoupon = Coupon.builder()
                .code(couponCode)
                .title("Happy Birthday!")
                .description("Special birthday discount just for you!")
                .discountType(DiscountType.PERCENT)
                .discountValue(10.0) // 10% discount
                .maxDiscountAmount(100000.0) // Max 100k VND
                .minOrderAmount(50000.0) // Min 50k VND
                .maxUsage(1)
                .usedCount(0)
                .status(CouponStatus.ACTIVE)
                .couponType(CouponType.BIRTHDAY)
                .applicableUsers(Arrays.asList(user))
                .build();

        couponRepository.save(birthdayCoupon);
    }

    @Override
    public void createFirstOrderCouponForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId, "USER_NOT_FOUND"));

        String couponCode = "WELCOME" + userId;

        // Check if welcome coupon already exists
        if (couponRepository.findByCode(couponCode).isPresent()) {
            return; // Already created
        }

        Coupon welcomeCoupon = Coupon.builder()
                .code(couponCode)
                .title("Welcome to FoodOrder!")
                .description("Get discount on your first order!")
                .discountType(DiscountType.AMOUNT)
                .discountValue(30000.0) // 30k VND discount
                .minOrderAmount(100000.0) // Min 100k VND
                .maxUsage(1)
                .usedCount(0)
                .status(CouponStatus.ACTIVE)
                .couponType(CouponType.FIRST_ORDER)
                .applicableUsers(Arrays.asList(user))
                .build();

        couponRepository.save(welcomeCoupon);
    }

    @Override
    public void sendPrivateCouponToUsers(Long couponId, List<Long> userIds) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found with id: " + couponId, "COUPON_NOT_FOUND"));

        List<User> users = userRepository.findAllById(userIds);

        // Add users to applicable users list
        if (coupon.getApplicableUsers() == null) {
            coupon.setApplicableUsers(new ArrayList<>());
        }

        coupon.getApplicableUsers().addAll(users);
        coupon.setCouponType(CouponType.PRIVATE);

        couponRepository.save(coupon);
    }

    // === HELPER METHODS ===

    private void validateCouponRequest(CouponRequest request) {
        // Additional business validations beyond Bean Validation
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new BadRequestException("Start date must be before end date", "INVALID_DATE_RANGE");
        }

        if (request.getDiscountType() == DiscountType.PERCENT) {
            if (request.getDiscountValue() > 100) {
                throw new BadRequestException("Percentage discount cannot exceed 100%", "INVALID_DISCOUNT_PERCENT");
            }
            if (request.getMaxDiscountAmount() == null) {
                throw new BadRequestException("Max discount amount is required for percentage discounts", "MAX_DISCOUNT_REQUIRED");
            }
        }

        if (request.getCouponType() == CouponType.PRIVATE) {
            if (request.getApplicableUserIds() == null || request.getApplicableUserIds().isEmpty()) {
                throw new BadRequestException("Applicable user IDs are required for private coupons", "APPLICABLE_USERS_REQUIRED");
            }
        }
    }

    private void setApplicableEntities(Coupon coupon, CouponRequest request) {
        // Set applicable categories
        if (request.getApplicableCategoryIds() != null && !request.getApplicableCategoryIds().isEmpty()) {
            coupon.setApplicableCategories(
                categoryRepository.findAllById(request.getApplicableCategoryIds())
            );
        }

        // Set applicable foods
        if (request.getApplicableFoodIds() != null && !request.getApplicableFoodIds().isEmpty()) {
            coupon.setApplicableFoods(
                foodRepository.findAllById(request.getApplicableFoodIds())
            );
        }

        // Set applicable users (for private coupons)
        if (request.getApplicableUserIds() != null && !request.getApplicableUserIds().isEmpty()) {
            coupon.setApplicableUsers(
                userRepository.findAllById(request.getApplicableUserIds())
            );
        }
    }
}
