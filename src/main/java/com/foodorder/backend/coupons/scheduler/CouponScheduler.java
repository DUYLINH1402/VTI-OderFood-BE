package com.foodorder.backend.coupons.scheduler;

import com.foodorder.backend.coupons.service.CouponService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled Tasks cho Coupon System
 * Tự động hóa các tác vụ như cập nhật trạng thái, tạo coupon sinh nhật...
 */
@Component
@Slf4j
public class CouponScheduler {

    @Autowired
    private CouponService couponService;

    /**
     * Cập nhật trạng thái coupon hết hạn
     * Chạy mỗi ngày lúc 1:00 AM
     */
    @Scheduled(cron = "0 0 1 * * *")
    public void updateExpiredCoupons() {
        log.info("Starting scheduled task: Update expired coupons");
        try {
            couponService.updateExpiredCoupons();
            log.info("Successfully updated expired coupons");
        } catch (Exception e) {
            log.error("Error updating expired coupons: {}", e.getMessage(), e);
        }
    }

    /**
     * Cập nhật trạng thái coupon hết lượt sử dụng
     * Chạy mỗi ngày lúc 2:00 AM
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void updateUsedOutCoupons() {
        log.info("Starting scheduled task: Update used out coupons");
        try {
            couponService.updateUsedOutCoupons();
            log.info("Successfully updated used out coupons");
        } catch (Exception e) {
            log.error("Error updating used out coupons: {}", e.getMessage(), e);
        }
    }

    /**
     * Gửi thông báo coupon sắp hết hạn
     * Chạy mỗi ngày lúc 9:00 AM
     */
    @Scheduled(cron = "0 0 9 * * *")
    public void notifyExpiringSoonCoupons() {
        log.info("Starting scheduled task: Notify expiring soon coupons");
        try {
            var expiringSoonCoupons = couponService.getExpiringSoonCoupons(3); // 3 ngày
            if (!expiringSoonCoupons.isEmpty()) {
                log.warn("Found {} coupons expiring in 3 days", expiringSoonCoupons.size());
                // TODO: Implement notification logic (email, push notification, etc.)
                // notificationService.sendExpiringCouponsAlert(expiringSoonCoupons);
            }
            log.info("Successfully processed expiring soon coupons notification");
        } catch (Exception e) {
            log.error("Error processing expiring soon coupons: {}", e.getMessage(), e);
        }
    }

    /**
     * Làm sạch dữ liệu coupon usage cũ (optional)
     * Chạy mỗi tháng vào ngày 1 lúc 3:00 AM
     */
    @Scheduled(cron = "0 0 3 1 * *")
    public void cleanupOldCouponUsage() {
        log.info("Starting scheduled task: Cleanup old coupon usage data");
        try {
            // TODO: Implement cleanup logic for very old usage records (6+ months)
            // couponUsageService.cleanupOldUsageRecords();
            log.info("Successfully cleaned up old coupon usage data");
        } catch (Exception e) {
            log.error("Error cleaning up old coupon usage data: {}", e.getMessage(), e);
        }
    }

    /**
     * Thống kê và báo cáo hàng tuần
     * Chạy mỗi Chủ nhật lúc 23:00
     */
    @Scheduled(cron = "0 0 23 * * SUN")
    public void generateWeeklyReport() {
        log.info("Starting scheduled task: Generate weekly coupon report");
        try {
            var statistics = couponService.getCouponStatistics();
            var mostUsedCoupons = couponService.getMostUsedCoupons(10);

            log.info("Weekly Coupon Statistics: {}", statistics);
            log.info("Top 10 most used coupons this week: {}", mostUsedCoupons.size());

            // TODO: Send weekly report to admin
            // reportService.sendWeeklyCouponReport(statistics, mostUsedCoupons);

            log.info("Successfully generated weekly coupon report");
        } catch (Exception e) {
            log.error("Error generating weekly coupon report: {}", e.getMessage(), e);
        }
    }
}
