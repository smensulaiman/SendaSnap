package com.sendajapan.sendasnap.utils;

import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyFormatter {
    public static String formatBuyingPrice(String priceStr) {
        double price = 0.0;
        if (priceStr != null && !priceStr.isEmpty()) {
            try {
                price = Double.parseDouble(priceStr);
            } catch (NumberFormatException ignored) {}
        }
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.getDefault());
        formatter.setMaximumFractionDigits(0);
        formatter.setGroupingUsed(true);
        return formatter.format(price);
    }
}
