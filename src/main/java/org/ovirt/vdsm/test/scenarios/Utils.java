package org.ovirt.vdsm.test.scenarios;

import java.security.GeneralSecurityException;
import java.util.Random;

public final class Utils {

    private final static String[] MAC =
            { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F" };

    public static String generateMacAddress() {
        Random rd = new Random();
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < 6; i++) {
            result.append(MAC[rd.nextInt(15)] + MAC[rd.nextInt(15)]);
            if (i < 5) {
                result.append(":");
            }
        }
        return result.toString();
    }

    public static void validate(String path) throws GeneralSecurityException {
        if (path == null || "".equals(path.trim())) {
            throw new GeneralSecurityException("Configuration file not found");
        }
    }

    public static String removeQuotes(String value) {
        if (value.startsWith("\"")) {
            value = value.substring(1);
        }
        if (value.endsWith("\"")) {
            int len = value.length();
            value = value.substring(0, len - 1);
        }
        return value;
    }
}
