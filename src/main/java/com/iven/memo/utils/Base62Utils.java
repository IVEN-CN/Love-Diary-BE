package com.iven.memo.utils;

public class Base62Utils {
    // 标准的 Base62 字符表：0-9, A-Z, a-z
    private static final char[] BASE62_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();
    private static final int BASE = 62;

    /**
     * 将十进制的长整型ID编码为Base62字符串
     * @param decimalId 数据库自增ID（正数）
     * @return Base62编码后的字符串
     */
    public static String encode(long decimalId) {
        if (decimalId == 0) {
            return "0";
        }
        if (decimalId < 0) {
            throw new IllegalArgumentException("ID must be non-negative");
        }

        StringBuilder sb = new StringBuilder();
        // 核心算法：不断除以62取余数，并查找对应字符
        while (decimalId > 0) {
            int remainder = (int) (decimalId % BASE); // 余数作为字符表索引
            sb.append(BASE62_CHARS[remainder]);
            decimalId = decimalId / BASE; // 商进入下一轮计算
        }
        // 因为是从低位开始取的，所以需要反转字符串
        return sb.reverse().toString();
    }

    /**
     * 将Base62字符串解码为十进制的长整型ID
     * @param base62Str Base62编码的字符串
     * @return 解码后的十进制ID
     * @throws IllegalArgumentException 如果输入字符串包含非法字符
     */
    public static long decode(String base62Str) {
        if (base62Str == null || base62Str.isBlank()) {
            throw new IllegalArgumentException("Input string cannot be null or empty");
        }

        long result = 0;
        long power = 1; // 从最低位开始，代表62的0次方

        // 从字符串最右侧（最低位）开始向左扫描
        for (int i = base62Str.length() - 1; i >= 0; i--) {
            int digit = getDigit(base62Str, i);

            result += digit * power;
            power *= BASE; // 每向左移动一位，幂次乘62
        }
        return result;
    }

    private static int getDigit(String base62Str, int i) {
        char c = base62Str.charAt(i);
        int digit = -1;

        // 查找字符在字符表中的位置（即对应的数值）
        if (c >= '0' && c <= '9') {
            digit = c - '0';
        } else if (c >= 'A' && c <= 'Z') {
            digit = 10 + (c - 'A');
        } else if (c >= 'a' && c <= 'z') {
            digit = 36 + (c - 'a');
        }

        if (digit == -1) {
            throw new IllegalArgumentException("Invalid character '" + c + "' in Base62 string");
        }
        return digit;
    }
}
