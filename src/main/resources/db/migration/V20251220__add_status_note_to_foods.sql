-- Migration: Thêm cột status_note vào bảng foods
-- Mục đích: Cho phép Staff ghi chú lý do thay đổi trạng thái món ăn (hết hàng, bảo trì...)

ALTER TABLE foods ADD COLUMN status_note VARCHAR(500) NULL;

-- Thêm comment cho cột
COMMENT ON COLUMN foods.status_note IS 'Ghi chú lý do thay đổi trạng thái món ăn (VD: hết nguyên liệu, bảo trì...)';

