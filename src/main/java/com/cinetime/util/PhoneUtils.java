package com.cinetime.util;

import com.google.i18n.phonenumbers.*;

public final class PhoneUtils {
    private static final PhoneNumberUtil UTIL = PhoneNumberUtil.getInstance();

    private PhoneUtils() {}

    /** raw: kullanıcıdan gelen numara, region: "TR", "DE", "AT" vb. */
    public static String toE164(String raw, String region) {
        if (raw == null || raw.isBlank()) return null;
        try {
            Phonenumber.PhoneNumber num = UTIL.parse(raw, region);
            if (!UTIL.isValidNumber(num)) {
                throw new IllegalArgumentException("Telefon numarası geçersiz");
            }
            return UTIL.format(num, PhoneNumberUtil.PhoneNumberFormat.E164); // +90...
        } catch (NumberParseException e) {
            throw new IllegalArgumentException("Telefon numarası geçersiz", e);
        }
    }
}
