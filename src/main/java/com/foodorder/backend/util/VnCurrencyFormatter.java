package com.foodorder.backend.util;

import java.text.NumberFormat;
import java.util.Locale;

public class VnCurrencyFormatter {
    public static String format(long amount) {
        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        return nf.format(amount) + " VNĐ";
    }
}