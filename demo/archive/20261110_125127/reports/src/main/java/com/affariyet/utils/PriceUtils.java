package com.affariyet.utils;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * Conversion des prix affichés par Affariyet (ex. « 2 369,000 TND ») en nombres.
 */
public final class PriceUtils {

    /**
     * Format observé dans le HTML du site : « 2 369,000 TND »
     * (espace fine insécable entre les milliers, espace insécable avant TND, 3 décimales).
     */
    public static final Pattern PRICE_FORMAT =
            Pattern.compile("^\\d{1,3}([\\s\\u00A0\\u202F]\\d{3})*,\\d{3}[\\s\\u00A0\\u202F]?TND$");

    private PriceUtils() {
    }

    public static BigDecimal parse(String displayed) {
        if (displayed == null) {
            throw new IllegalArgumentException("Prix null");
        }
        String digits = displayed.replaceAll("[^0-9,]", "").replace(',', '.');
        if (digits.isEmpty()) {
            throw new IllegalArgumentException("Prix illisible : '" + displayed + "'");
        }
        return new BigDecimal(digits);
    }

    public static boolean hasValidFormat(String displayed) {
        return displayed != null && PRICE_FORMAT.matcher(displayed.strip()).matches();
    }
}
