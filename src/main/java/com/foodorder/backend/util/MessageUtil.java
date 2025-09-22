package com.foodorder.backend.util;

import com.foodorder.backend.order.entity.OrderStatus;

/**
 * Utility class để tạo các message thân thiện cho người dùng về trạng thái đơn hàng
 */
public class MessageUtil {

    /**
     * Tạo message thông báo trạng thái đơn hàng cho khách hàng
     * @param orderCode Mã đơn hàng
     * @param newStatus Trạng thái mới
     * @param oldStatus Trạng thái cũ (có thể null)
     * @return Message thân thiện cho khách hàng
     */
    public static String createCustomerMessage(String orderCode, String newStatus, String oldStatus) {
        if (orderCode == null || newStatus == null) {
            return "Đơn hàng của bạn đã được cập nhật.";
        }

        try {
            OrderStatus status = OrderStatus.valueOf(newStatus);

            switch (status) {
                case PENDING:
                    return String.format("Đơn hàng %s đang chờ thanh toán. Vui lòng hoàn tất thanh toán để chúng tôi xử lý đơn hàng.", orderCode);

                case PROCESSING:
                    return String.format("Đơn hàng %s đã được thanh toán thành công! Đang chờ nhân viên xác nhận", orderCode);

                case CONFIRMED:
                    return String.format("Đơn hàng %s đã được xác nhận và đang được chế biến. Món ăn ngon đang được chuẩn bị cho bạn!", orderCode);

                case DELIVERING:
                    return String.format("Đơn hàng %s được chuẩn bị xong! Vui lòng chuẩn bị nhận hàng.", orderCode);

                case COMPLETED:
                    return String.format("Đơn hàng %s đã được giao thành công! Cảm ơn bạn đã sử dụng dịch vụ. Hãy đánh giá cho chúng tôi nhé!", orderCode);

                case CANCELLED:
                    return String.format("Đơn hàng %s đã bị hủy. Nếu bạn đã thanh toán, chúng tôi sẽ hoàn tiền trong 1-3 ngày làm việc.", orderCode);

                default:
                    return String.format("Đơn hàng %s đã được cập nhật trạng thái: %s", orderCode, status.getDescription());
            }
        } catch (IllegalArgumentException e) {
            return String.format("Đơn hàng %s đã được cập nhật.", orderCode);
        }
    }

    /**
     * Tạo message thông báo cho staff/admin
     * @param orderCode Mã đơn hàng
     * @param newStatus Trạng thái mới
     * @param oldStatus Trạng thái cũ
     * @param customerName Tên khách hàng
     * @return Message cho staff
     */
    public static String createStaffMessage(String orderCode, String newStatus, String oldStatus, String customerName) {
        if (orderCode == null || newStatus == null) {
            return "Có cập nhật đơn hàng mới";
        }

        String customerInfo = customerName != null ? " (KH: " + customerName + ")" : "";

        try {
            OrderStatus newStatusEnum = OrderStatus.valueOf(newStatus);

            if (oldStatus != null) {
                try {
                    OrderStatus oldStatusEnum = OrderStatus.valueOf(oldStatus);
                    return String.format("Đơn hàng %s%s đã chuyển từ \"%s\" thành \"%s\"",
                            orderCode, customerInfo, oldStatusEnum.getDescription(), newStatusEnum.getDescription());
                } catch (IllegalArgumentException e) {
                    return String.format("Đơn hàng %s%s đã chuyển thành \"%s\"",
                            orderCode, customerInfo, newStatusEnum.getDescription());
                }
            } else {
                return String.format("Đơn hàng mới %s%s - Trạng thái: %s",
                        orderCode, customerInfo, newStatusEnum.getDescription());
            }
        } catch (IllegalArgumentException e) {
            return String.format("Đơn hàng %s%s đã được cập nhật", orderCode, customerInfo);
        }
    }

    /**
     * Tạo message ngắn gọn cho notification
     * @param orderCode Mã đơn hàng
     * @param newStatus Trạng thái mới
     * @return Message ngắn gọn
     */
    public static String createShortMessage(String orderCode, String newStatus) {
        if (orderCode == null || newStatus == null) {
            return "Cập nhật đơn hàng";
        }

        try {
            OrderStatus status = OrderStatus.valueOf(newStatus);
            return String.format("%s: %s", orderCode, status.getDescription());
        } catch (IllegalArgumentException e) {
            return String.format("%s: %s", orderCode, newStatus);
        }
    }
}
