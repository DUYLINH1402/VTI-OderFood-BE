package com.foodorder.backend.util;

import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 * Utility class để tạo slug URL thân thiện từ chuỗi tiếng Việt
 */
public class SlugUtils {

    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");
    private static final Pattern MULTIPLE_DASH = Pattern.compile("-+");

    /**
     * Chuyển đổi chuỗi tiếng Việt thành slug URL thân thiện
     * Ví dụ: "Khuyến mãi tháng 1" -> "khuyen-mai-thang-1"
     *
     * @param input Chuỗi đầu vào (có thể là tiếng Việt)
     * @return Slug URL thân thiện
     */
    public static String toSlug(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }

        // Chuyển về chữ thường
        String result = input.toLowerCase().trim();

        // Chuyển đổi các ký tự tiếng Việt có dấu thành không dấu
        result = removeVietnameseAccents(result);

        // Normalize để loại bỏ các ký tự đặc biệt còn sót
        result = Normalizer.normalize(result, Normalizer.Form.NFD);
        result = result.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        // Thay khoảng trắng bằng dấu gạch ngang
        result = WHITESPACE.matcher(result).replaceAll("-");

        // Loại bỏ các ký tự không phải chữ cái, số, hoặc gạch ngang
        result = NONLATIN.matcher(result).replaceAll("");

        // Loại bỏ các dấu gạch ngang liên tiếp
        result = MULTIPLE_DASH.matcher(result).replaceAll("-");

        // Loại bỏ dấu gạch ngang ở đầu và cuối
        result = result.replaceAll("^-|-$", "");

        return result;
    }

    /**
     * Chuyển đổi ký tự tiếng Việt có dấu thành không dấu
     */
    private static String removeVietnameseAccents(String input) {
        String result = input;

        // Nguyên âm a
        result = result.replaceAll("[àáạảãâầấậẩẫăằắặẳẵ]", "a");
        // Nguyên âm e
        result = result.replaceAll("[èéẹẻẽêềếệểễ]", "e");
        // Nguyên âm i
        result = result.replaceAll("[ìíịỉĩ]", "i");
        // Nguyên âm o
        result = result.replaceAll("[òóọỏõôồốộổỗơờớợởỡ]", "o");
        // Nguyên âm u
        result = result.replaceAll("[ùúụủũưừứựửữ]", "u");
        // Nguyên âm y
        result = result.replaceAll("[ỳýỵỷỹ]", "y");
        // Chữ đ
        result = result.replaceAll("đ", "d");

        return result;
    }

    /**
     * Tạo slug unique bằng cách thêm suffix số
     *
     * @param baseSlug Slug gốc
     * @param suffix   Số suffix cần thêm
     * @return Slug với suffix
     */
    public static String toSlugWithSuffix(String baseSlug, int suffix) {
        return baseSlug + "-" + suffix;
    }
}

